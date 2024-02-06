package fr.epita.rloic.fr.epita.rloic.minizinc

class ProcessResult {
    data class Async(
        val stdout: Sequence<String>,
        val stderr: Sequence<String>,
        val waitFor: () -> Int
    )

    data class Sync(
        val stdout: String,
        val stderr: String,
        val returnCode: Int
    )
}