package fr.epita.rloic.fr.epita.rloic.minizinc

val defaultDriver get() = Driver.find() ?: throw NoSuchElementException("MiniZinc was not found on the system. No default driver could be initialized.")