package fr.epita.rloic.fr.epita.rloic.minizinc.utils

import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString

sealed class Configuration : AutoCloseable {
    abstract fun asString(): String

    class Str(private val value: String) : Configuration() {
        override fun close() {}
        override fun asString() = value
    }

    class TmpFile(private val path: Path) : Configuration() {
        override fun close() {
            path.deleteIfExists()
        }
        override fun asString() = path.pathString
    }
}



