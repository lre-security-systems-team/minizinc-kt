package fr.epita.rloic.fr.epita.rloic.minizinc

data class ProcessResult(
    val stdout: String,
    val stderr: String,
    val returnCode: Int
)

