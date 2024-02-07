package fr.epita.rloic.fr.epita.rloic.minizinc


data class SearchResult<T>(
    val status: Status,
    val solution: Solution<T>?,
    val statistics: Statistics
)