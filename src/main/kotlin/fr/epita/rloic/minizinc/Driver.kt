package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString


val CLI_REQUIRED_VERSION = Version(2, 6, 0)

val MAC_LOCATIONS = listOf(
    Path("/Applications/MiniZincIDE.app/Contents/Resources"),
    Path("~/Applications/MiniZincIDE.app/Contents/Resources").expandsUser()
)

val WIN_LOCATIONS = listOf(
    Path("c:/Program Files/MiniZinc"),
    Path("c:/Program Files/MiniZinc IDE (bundled)"),
    Path("c:/Program Files (x86)/MiniZinc"),
    Path("c:/Program Files (x86)/MiniZinc IDE (bundled)"),
)

class Driver private constructor(private val executable: Path) {

    companion object {
        fun find(
            path: List<Path>? = null,
            name: String = "minizinc"
        ): Driver? {
            @Suppress("NAME_SHADOWING")
            val path = (path ?: getDefaultPath()).toMutableList()

            if (system().contains("mac", true)) {
                path += MAC_LOCATIONS
            } else if (system().contains("win", true)) {
                path += WIN_LOCATIONS
            }

            val executable = which(name, path)
            return if (executable == null) {
                null
            } else {
                Driver(executable)
            }
        }
    }

    private val minizincVersion by lazy {
        val output = ProcessBuilder(listOf(executable.pathString, "--version"))
            .run()

        val version = output.stdout
        val regex = "version (\\d+)\\.(\\d+)\\.(\\d+)".toRegex()
        val match = regex.find(version)!!.groups
        Version(
            match[1]!!.value.toInt(),
            match[2]!!.value.toInt(),
            match[3]!!.value.toInt()
        )
    }

    private var solverCache: MutableMap<String, MutableList<Solver>>? = null

    init {
        if (!executable.exists()) {
            throw IllegalArgumentException("No MiniZinc executable was found at $executable.")
        }
        if (minizincVersion < CLI_REQUIRED_VERSION) {
            throw IllegalArgumentException("The MiniZinc driver found at $executable has version $minizincVersion. The minimal required version is $CLI_REQUIRED_VERSION")
        }
    }


    fun run(args: MutableList<String>, solver: Solver? = null): ProcessResult {
        // if (minizincVersion >= Version(2, 6, 0)) {
        args += "--json-stream"
        // }

        val output = if (solver == null) {
            val cmd = listOf(
                executable.pathString,
                "--allow-multiple-assignments"
            ) + args
            ProcessBuilder(cmd).run()
        } else {
            fun executeWithConf(conf: Configuration): ProcessResult {
                val cmd = listOf(
                    executable.pathString,
                    "--solver",
                    conf.asString(),
                    "--allow-multiple-assignments"
                ) + args
                return ProcessBuilder(cmd).run()
            }
            solver.configuration().use(::executeWithConf)
        }
        if (output.returnCode != 0) {
            throw parseError(output.stdout) ?: RuntimeException(output.stderr)
        }
        return output
    }

    fun runAsync(args: MutableList<String>, solver: Solver? = null): ASyncProcessResult {
        // if (minizincVersion >= Version(2, 6, 0)) {
        args += "--json-stream"
        // }

        val output = if (solver == null) {
            val cmd = listOf(
                executable.pathString,
                "--allow-multiple-assignments"
            ) + args
            ProcessBuilder(cmd).runAsync()
        } else {
            fun executeWithConf(conf: Configuration): ASyncProcessResult {
                val cmd = listOf(
                    executable.pathString,
                    "--solver",
                    conf.asString(),
                    "--allow-multiple-assignments"
                ) + args
                return ProcessBuilder(cmd).runAsync()
            }
            solver.configuration().use(::executeWithConf)
        }
        return output
    }

    fun availableSolvers(refresh: Boolean = false): Map<String, List<Solver>> {
        val currentSolverCache = solverCache
        if (!refresh && currentSolverCache != null) {
            return currentSolverCache
        }

        val output = run(mutableListOf("--solvers-json"))
        val solvers = loads<List<Solver>>(output.stdout)

        val solverCache = mutableMapOf<String, MutableList<Solver>>()
        for (s in solvers) {
            val obj = s
            if (obj.version == "<unknown version>") {
                obj._identifier = obj.id
            } else {
                obj._identifier = obj.id + "@" + obj.version
            }

            val names = s.tags.toMutableList()
            names += listOf(
                s.id, s.id.split('.').last()
            )
            for (name in names) {
                solverCache.getOrPut(name, ::mutableListOf) += obj
            }
        }
        this.solverCache = solverCache
        return solverCache
    }
}

@Serializable
data class Location(
    val filename: String? = null,
    val firstLine: Int = 0,
    val firstColumn: Int = 0,
    val lastLine: Int = 0,
    val lastColumn: Int = 0,
    val message: String = ""
)

@Serializable
class MznJsonError(
    val type: String = "error",
    val what: String = "",
    val location: Location,
    override val message: String = "",
) : RuntimeException()

fun parseError(text: String): MznJsonError? {
    return try { loads<MznJsonError>(text) } catch (_: Exception) { null }
}



