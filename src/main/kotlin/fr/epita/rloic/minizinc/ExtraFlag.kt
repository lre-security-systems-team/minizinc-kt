package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.serde.ExtraFlagSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ExtraFlagSerializer::class)
data class ExtraFlag(
    val name: String,
    val description: String,
    val type: String,
    val defaultValue: String
)