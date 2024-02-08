package fr.epita.rloic.fr.epita.rloic.minizinc.utils

import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString

sealed class Configuration {

    class Str(private val value: String) : Configuration() {
        override fun toString() = value
    }

    class TmpFile(private val path: Path) : Configuration() {
        override fun toString() = path.pathString
    }
}



