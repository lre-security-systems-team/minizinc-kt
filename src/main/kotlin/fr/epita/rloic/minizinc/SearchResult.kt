package fr.epita.rloic.fr.epita.rloic.minizinc

data class SearchResult(
    val status: Status,
    val solution: Solution?,
    val statistics: Statistics
)