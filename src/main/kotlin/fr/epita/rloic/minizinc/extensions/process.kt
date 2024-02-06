package fr.epita.rloic.fr.epita.rloic.minizinc.extensions

import fr.epita.rloic.fr.epita.rloic.minizinc.ProcessResult

fun Process.run() = ProcessResult.Sync(
    inputReader().readText(),
    errorReader().readText(),
    waitFor()
)

fun Process.runAsync() = ProcessResult.Async(
    inputReader().lineSequence(),
    errorReader().lineSequence(),
    this::waitFor
)

