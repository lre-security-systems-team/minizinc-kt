package fr.epita.rloic.fr.epita.rloic.minizinc

fun Process.run() = ProcessResult(
    inputReader().readText(),
    errorReader().readText(),
    waitFor()
)

fun Process.runAsync() = ASyncProcessResult(
    inputReader().lineSequence(),
    errorReader().lineSequence(),
    this::waitFor
)

