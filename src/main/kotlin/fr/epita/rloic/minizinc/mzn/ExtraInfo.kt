package fr.epita.rloic.fr.epita.rloic.minizinc.mzn

import fr.epita.rloic.fr.epita.rloic.minizinc.serde.PathSerializer
import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
data class ExtraInfo(
    val defaultFlags: List<String>? = null,
    val isDefault: Boolean? = null,
    @Serializable(with = PathSerializer::class) val mznlib: Path? = null,
    @Serializable(with = PathSerializer::class) val executable: Path? = null,
    @Serializable(with = PathSerializer::class) val configFile: Path? = null,
)