package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.extensions.expandsUser
import fr.epita.rloic.fr.epita.rloic.minizinc.extensions.run
import fr.epita.rloic.fr.epita.rloic.minizinc.extensions.runAsync
import fr.epita.rloic.fr.epita.rloic.minizinc.mzn.JsonOutput
import fr.epita.rloic.fr.epita.rloic.minizinc.mzn.Version
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import fr.epita.rloic.fr.epita.rloic.minizinc.utils.Configuration
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

    val version by lazy {
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
        if (version < CLI_REQUIRED_VERSION) {
            throw IllegalArgumentException("The MiniZinc driver found at $executable has version $version. The minimal required version is $CLI_REQUIRED_VERSION")
        }
    }


    fun run(
        args: MutableList<String>,
        solver: Solver? = null,
        lifeTime: Lifetime = Lifetime()
    ): ProcessResult.Sync {
        args += "--json-stream"

        val output = if (solver == null) {
            val cmd = listOf(
                executable.pathString,
                "--allow-multiple-assignments"
            ) + args
            ProcessBuilder(cmd).run()
        } else {
            fun executeWithConf(conf: Configuration): ProcessResult.Sync {
                val cmd = listOf(
                    executable.pathString,
                    "--solver",
                    conf.toString(),
                    "--allow-multiple-assignments"
                ) + args
                return ProcessBuilder(cmd).run()
            }
            executeWithConf(solver.configuration(lifeTime))
        }
        if (output.returnCode != 0) {
            parseError(output.stdout)?.throws() ?: throw RuntimeException(output.stderr)
        }
        return output
    }

    fun runAsync(
        args: MutableList<String>,
        solver: Solver? = null,
        lifetime: Lifetime = Lifetime()
    ): ProcessResult.Async =
        with(lifetime) {
            args += "--json-stream"

            val output = if (solver == null) {
                val cmd = listOf(
                    executable.pathString,
                    "--allow-multiple-assignments"
                ) + args
                ProcessBuilder(cmd).runAsync()
            } else {
                fun executeWithConf(conf: Configuration): ProcessResult.Async {
                    val cmd = listOf(
                        executable.pathString,
                        "--solver",
                        conf.toString(),
                        "--allow-multiple-assignments"
                    ) + args
                    return ProcessBuilder(cmd).runAsync()
                }
                executeWithConf(solver.configuration(this@with))
            }
            return output
        }

    fun availableSolvers(refresh: Boolean = false): Map<String, List<Solver>> {
        val currentSolverCache = solverCache
        if (!refresh && currentSolverCache != null) {
            return currentSolverCache
        }

        val output = run(mutableListOf("--solvers-json"))
        val solvers: List<Solver> = loads<List<Solver>>(output.stdout)

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
                solverCache.getOrPut(name) { mutableListOf<Solver>() }.add(obj)
            }
        }
        this.solverCache = solverCache
        return solverCache
    }
}

fun parseError(text: String): JsonOutput.Error? {
    return try {
        loads<JsonOutput>(text) as? JsonOutput.Error
    } catch (_: Exception) {
        null
    }
}



