package fr.epita.rloic.fr.epita.rloic.minizinc

data class ASyncProcessResult(
    val stdout: Sequence<String>,
    val stderr: Sequence<String>,
    val waitFor: () -> Int
)