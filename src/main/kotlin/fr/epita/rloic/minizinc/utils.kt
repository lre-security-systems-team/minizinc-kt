package fr.epita.rloic.fr.epita.rloic.minizinc

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile

fun getDefaultPath(): List<Path> {
    val path = System.getenv("PATH") ?: return emptyList()
    return path.split(File.pathSeparatorChar)
        .map(::Path)
}

fun which(name: String, path: List<Path>? = null): Path? {
    for (p in path ?: getDefaultPath()) {
        val currentPath = p.resolve(name)
        if (currentPath.exists() && currentPath.isRegularFile() && currentPath.isExecutable()) {
            return currentPath
        }
    }
    return null
}