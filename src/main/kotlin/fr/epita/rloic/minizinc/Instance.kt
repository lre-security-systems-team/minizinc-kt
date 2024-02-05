package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznData
import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznValue
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.dumps
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.appendText
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.time.Duration

class Instance(
    private val solver: Solver,
    private val model: Model,
    driver: Driver? = null,
    private val parent: Instance? = null
) {
    private val fieldRenames = mutableListOf<Pair<String, String>>()
    private val driver = driver ?: defaultDriver
    private val method: Method

    init {
        method = analyse().method
    }

    fun solve(
        timeLimit: Duration? = null,
        nrSolutions: Long? = null,
        processes: Int? = null,
        randomSeed: Long? = null,
        allSolutions: Boolean = false,
        intermediateSolutions: Boolean = false,
        freeSearch: Boolean = false,
        optimisationLevel: Int? = null,
        kwargs: Map<String, Any> = emptyMap()
    ): MznResult {
        var status = Status.UNKNOWN
        var solution: Solution? = null
        val statistics = Statistics()

        val multipleSolutions = (allSolutions || intermediateSolutions || (nrSolutions != null))
        if (multipleSolutions) {
            solution = Solution.MultipleSolutions()
        }

        for (result in solutions(
            timeLimit,
            nrSolutions,
            processes,
            randomSeed,
            allSolutions,
            intermediateSolutions,
            freeSearch,
            optimisationLevel,
            kwargs = kwargs
        )) {
            status = result.status
            statistics.update(result.statistics)
            if (result.solution != null) {
                if (solution is Solution.MultipleSolutions) {
                    solution += result.solution
                } else {
                    solution = result.solution
                }
            }
        }
        return MznResult(status, solution, statistics)
    }

    private fun solutions(
        timeLimit: Duration? = null,
        nrSolutions: Long? = null,
        processes: Int? = null,
        randomSeed: Long? = null,
        allSolutions: Boolean = false,
        intermediateSolutions: Boolean = false,
        freeSearch: Boolean = false,
        optimisationLevel: Int? = null,
        verbose: Boolean = false,
        debutOutput: Path? = null,
        kwargs: Map<String, Any> = emptyMap()
    ): Sequence<MznResult> = sequence {
        val cmd = mutableListOf(
            "--output-mode",
            "json",
            "--output-time",
            "--output-objective",
            "--output-output-item",
            "--statistics",
            "--intermediate-solutions"
        )

        if (allSolutions) {
            if (nrSolutions != null) {
                throw IllegalArgumentException(
                    """
                    The number of solutions cannot be limited when looking
                    for all solutions
                """.trimIndent()
                )
            }
            if (method != Method.SATISFY) {
                throw NotImplementedError(
                    """
                    Finding all optimal solutions is not yet implemented
                """.trimIndent()
                )
            }
            checkFlagSupport(solver, "-a")
            cmd.add("--all-solutions")
        } else if (nrSolutions != null) {
            if (nrSolutions <= 0L) {
                throw IllegalArgumentException(
                    """
                    The number of solutions can only be set to a positive
                    integer number
                """.trimIndent()
                )
            }
            if (method != Method.SATISFY) {
                throw NotImplementedError("Finding multiple optimal solutions is not yet implemented")
            }
            checkFlagSupport(solver, "-n")
            cmd += listOf("--num-solutions", nrSolutions.toString())
        }
        if (processes != null) cmd += listOf("--parallel", processes.toString())
        if (randomSeed != null) cmd += listOf("--random-seed", randomSeed.toString())
        if (freeSearch) cmd += "--free-search"
        if (optimisationLevel != null) cmd += listOf("-O", optimisationLevel.toString())
        if (timeLimit != null) cmd += listOf("--time-limit", timeLimit.inWholeSeconds.toString())
        if (verbose) cmd += "--verbose"

        for ((flag, value) in kwargs.entries) {
            val flag = if (flag.startsWith("-")) flag else "--$flag"
            if (value is Boolean) {
                cmd += flag
            } else {
                cmd += listOf(flag, value.toString())
            }
        }

        files().use { files ->
            cmd += files.map(Path::toString)
            var status = Status.UNKNOWN
            var statistics = Statistics()
            val proc = driver.runAsync(cmd, solver)

            for (line in proc.stdout) {
                val error = parseError(line)
                if (error != null) throw error

                val (solution, newStatus, _statistics) = parseStreamObj(loads(line), statistics)
                statistics = _statistics
                if (newStatus != null) {
                    status = newStatus
                } else if (solution != null) {
                    if (status == Status.UNKNOWN) {
                        status = Status.SATISFIED
                    }
                    yield(MznResult(status, solution, statistics))
                    statistics = Statistics()
                }
            }
            proc.waitFor()
        }
    }

    private fun files(): FilesContextManager {
        val files = mutableListOf<Path>()
        val fragments = mutableListOf<String>()
        val data = mutableMapOf<String, DznValue>()

        var inst: Instance? = this
        while (inst != null) {
            for ((k, v) in inst.model.data.entries) {
                if (v is DznValue.Enum) {
                    TODO()
                } else {
                    data[k] = v
                }
            }
            fragments += inst.model.codeFragments
            files += inst.model.includes
            inst = inst.parent
        }
        val genFiles = mutableListOf<Path>()
        if (data.isNotEmpty()) {
            val file = createTempFile(
                prefix = "mzn_data",
                suffix = ".json"
            )
            genFiles.add(file)
            file.writeText(dumps(DznData(data).toJsonObject()))

        }
        if (fragments.isNotEmpty() || files.isEmpty()) {
            val file = createTempFile(
                prefix = "mzn_fragment",
                suffix = ".mzn"
            )
            genFiles.add(file)
            for (code in fragments) {
                file.appendText(code)
            }
        }
        return FilesContextManager(files, genFiles)
    }

    private fun parseStreamObj(obj: MutableMap<String, JsonElement>, statistics: Statistics): Ret {
        var solution: Solution.Single? = null
        var status: Status? = null
        val type = (obj["type"] as? JsonPrimitive)?.content


        when (type) {
            "solution" -> {
                val tmp = DznData.fromJsonObject((obj["output"] as JsonObject)["json"] as JsonObject)
                val mznFieldsRename = listOf(
                    "_objective" to "objective",
                    "_output" to "_output_item"
                )
                for ((before, after) in mznFieldsRename + fieldRenames) {
                    if (before in tmp.keys) {
                        tmp[after] = tmp.remove(before)!!
                    }
                }

                solution = Solution.Single(tmp)
                statistics.time = timedelta((obj["time"] as JsonPrimitive).double)
            }

            "time" -> {
                statistics.time = timedelta((obj["time"] as JsonPrimitive).double)
            }

            "statistics" -> {
                // TODO: update statistics
                // System.err.println(loads<Statistics>(dumps(obj["statistics"])))
            }

            "status" -> {
                status = Status.valueOf((obj["status"] as JsonPrimitive).content)
            }

            "checker" -> {
                // TODO: handle checker
                System.err.println(obj)
            }
        }
        return Ret(solution, status, statistics)
    }

    private fun analyse(): MznInterface {
        return files().use { files ->
            loads<MznInterface>(
                driver.run(
                    (listOf("--model-interface-only") + files.map(Path::toString)).toMutableList(),
                    solver
                ).stdout
            )
        }
    }

}

@Serializable
data class MznInterface(
    val method: Method
)

fun checkFlagSupport(solver: Solver, flag: String) {
    if (flag !in solver.stdFlags) throwUnsupportedFlag(flag)
}

fun throwUnsupportedFlag(flag: String): Nothing = throw NotImplementedError("Solver does not support the $flag flag")

enum class Status {
    ERROR, UNKNOWN, UNBOUNDED, UNSATISFIABLE, SATISFIED, ALL_SOLUTIONS, OPTIMAL_SOLUTION;
}

data class Ret(
    val newSolution: Solution.Single?,
    val newStatus: Status?,
    val updatedStatistics: Statistics
)

fun timedelta(instantMilli: Double): Double {
    return Instant.now().toEpochMilli() - instantMilli
}
