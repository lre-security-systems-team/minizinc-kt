package fr.epita.rloic.fr.epita.rloic.minizinc.mzn

import kotlinx.serialization.Serializable

@Serializable
data class StackElement(
    val location: Location? = null,
    val isCompIter: Boolean? = null,
    val description: String? = null
) {

    override fun toString() = buildString {
        if (location != null) {
            append("location: ")
            append(location)
        }
        if (isCompIter != null) {
            if (isNotEmpty()) append("\n   ")
            append("isCompIter: ")
            append(isCompIter)
        }
        if (description != null) {
            if (isNotEmpty()) append("\n   ")
            append("description: ")
            append(description)
        }
    }

}