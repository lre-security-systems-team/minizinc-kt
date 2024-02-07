package fr.epita.rloic.fr.epita.rloic.minizinc.mzn

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val filename: String? = null,
    val firstLine: Int? = null,
    val firstColumn: Int? = null,
    val lastLine: Int? = null,
    val lastColumn: Int? = null,
    val message: String? = null
) {
    override fun toString() = buildString {
        if (filename != null) {
            append("filename: ")
            append(filename)
        }
        if (listOf(firstLine, firstColumn, lastLine, lastColumn).any { it != 0 && it != null }) {
            if (isNotEmpty()) append(' ')
            append("from ")
            append(firstLine)
            append(':')
            append(firstColumn)
            append(" to ")
            append(lastLine)
            append(':')
            append(lastColumn)
        }
        if (!message.isNullOrBlank()) {
            if(isNotEmpty()) append(' ')
            append("message: ")
            append(message)
        }
        if (isEmpty()) append("null")
    }
}