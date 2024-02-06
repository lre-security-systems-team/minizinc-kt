package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznData
import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznValue
import fr.epita.rloic.fr.epita.rloic.minizinc.mzn.Method
import fr.epita.rloic.fr.epita.rloic.minizinc.mzn.JsonOutput
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.dumps
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import fr.epita.rloic.fr.epita.rloic.minizinc.utils.FilesContextManager
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.appendText
import kotlin.io.path.createTempFile
import kotlin.io.path.pathString
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
    ): SearchResult {
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
        return SearchResult(status, solution, statistics)
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
    ): Sequence<SearchResult> = sequence {
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

        val contextManager = ContextManager()
        files().use { files ->
            cmd += files.map(Path::toString)
            var status = Status.UNKNOWN
            var statistics = Statistics()
            val proc = driver.runAsync(cmd, solver, contextManager)

            for (line in proc.stdout) {
                parseError(line)?.throws()

                val (solution, newStatus, _statistics) = parseStreamObj(loads(line), statistics)
                statistics = _statistics
                if (newStatus != null) {
                    status = newStatus
                } else if (solution != null) {
                    if (status == Status.UNKNOWN) {
                        status = Status.SATISFIED
                    }
                    yield(SearchResult(status, solution, statistics))
                    statistics = Statistics()
                }
            }
            proc.waitFor()
        }
    }

    private fun flat(
        timeLimit: Duration? = null,
        optimizationLevel: Int? = null
    ): FilesContextManager {

        val cmd = mutableListOf("--compile", "--statistics")

        val fzn = createTempFile(
            prefix = "fzn_",
            suffix = ".fzn"
        )
        cmd += listOf("--fzn", fzn.pathString)
        val ozn = createTempFile(
            prefix = "ozn_",
            suffix = ".ozn"
        )
        cmd += listOf("--ozn", ozn.pathString)


        if(timeLimit != null) {
            cmd += listOf("--time-limit", timeLimit.inWholeMilliseconds.toString())
        }

        if (optimizationLevel != null) {
            cmd += listOf("-O", optimizationLevel.toString())
        }


        return FilesContextManager(emptyList(), listOf(fzn, ozn))

        TODO()
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

    data class UpdatedResult(
        val newSolution: Solution.Single?,
        val newStatus: Status?,
        val updatedStatistics: Statistics
    )

    private fun parseStreamObj(obj: JsonOutput, statistics: Statistics): UpdatedResult {
        var solution: Solution.Single? = null
        var status: Status? = null

        when (obj) {
            is JsonOutput.Interface -> {
                // TODO: print warning?
            }

            is JsonOutput.Comment -> {
                System.err.print(obj.comment)
            }

            is JsonOutput.Error -> {
                throw RuntimeException(obj.message)
            }

            is JsonOutput.Solution -> {
                val tmp = DznData.fromJsonObject(obj.output.json)
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
                statistics.time = timedelta(obj.time)
            }

            is JsonOutput.Time -> {
                statistics.time = timedelta(obj.time)
            }

            is JsonOutput.Statistics -> {
                // TODO: update statistics
                // System.err.println(loads<Statistics>(dumps(obj["statistics"])))
            }

            is JsonOutput.Status -> {
                status = obj.status
            }

            is JsonOutput.Checker -> {
                // TODO: handle checker
                System.err.println(obj)
            }
        }
        return UpdatedResult(solution, status, statistics)
    }

    private fun analyse(): JsonOutput.Interface {
        return files().use { files ->
            loads<JsonOutput>(
                driver.run(
                    (listOf("--model-interface-only") + files.map(Path::toString)).toMutableList(),
                    solver
                ).stdout
            ) as JsonOutput.Interface
        }
    }

}


fun checkFlagSupport(solver: Solver, flag: String) {
    if (flag !in solver.stdFlags) throwUnsupportedFlag(flag)
}

fun throwUnsupportedFlag(flag: String): Nothing = throw NotImplementedError("Solver does not support the $flag flag")



fun timedelta(instantMilli: Double): Double {
    return Instant.now().toEpochMilli() - instantMilli
}
