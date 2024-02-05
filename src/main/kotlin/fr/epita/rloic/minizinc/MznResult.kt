package fr.epita.rloic.fr.epita.rloic.minizinc

data class MznResult(
    val status: Status,
    val solution: Solution?,
    val statistics: Statistics
)