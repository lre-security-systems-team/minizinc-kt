package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.serde.PathSerializer
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.dumps
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import kotlinx.serialization.Serializable
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.*

@Serializable
data class Solver(
    val name: String,
    val version: String,
    val id: String,
    @Serializable(with = PathSerializer::class)
    val executable: Path? = null,
    @Serializable(with = PathSerializer::class)
    val mznlib: Path? = null,
    val mznlibVersion: Int = 1,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val stdFlags: List<String> = emptyList(),
    val extraFlags: List<ExtraFlag> = emptyList(),
    val requiredFlags: List<String> = emptyList(),
    val inputType: String = "FZN",
    val supportsMzn: Boolean = false,
    val supportsFzn: Boolean = true,
    val supportsNL: Boolean = false,
    val needsSolns2Out: Boolean = false,
    val needsMznExecutable: Boolean = false,
    val needsStdlibDir: Boolean = false,
    val needsPathsFile: Boolean = false,
    val isGUIApplication: Boolean = false,
    var _identifier: String? = null
) {

    companion object {
        fun lookup(tag: String, driver: Driver? = null, refresh: Boolean = false): Solver {
            val driver = driver ?: defaultDriver

            val tagMap = driver.availableSolvers(refresh)

            val solver = tagMap[tag]?.firstOrNull()
                ?: throw NoSuchElementException("No solver id or tag $tag found, available options: ${tagMap.keys.sorted()}")
            return solver
        }

        fun load(path: Path): Solver {
            if (!path.exists()) {
                throw FileNotFoundException(path.pathString)

            }
            val solver = loads<Solver>(path.readText())
            // TODO: "Resolve relative paths"
            return solver
        }
    }

    fun configuration(): Configuration {
        val identifier = _identifier
        return if (identifier != null) {
            Configuration.Str(identifier)
        } else {
            val path = createTempFile(
                prefix = "minizinc_solver_",
                suffix = ".msc",
            )
            path.writeText(outputConfiguration())
            Configuration.TmpFile(path)
        }
    }

    private fun outputConfiguration(): String = dumps(this)

}

