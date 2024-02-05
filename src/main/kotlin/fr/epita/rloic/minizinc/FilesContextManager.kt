package fr.epita.rloic.fr.epita.rloic.minizinc

import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class FilesContextManager(
    private val nonManagedPaths: List<Path>,
    private val managedPaths: List<Path>
) : AutoCloseable, Iterable<Path> {
    override fun close() {
        managedPaths.forEach(Path::deleteIfExists)
    }
    override fun iterator(): Iterator<Path> =JoinIterator(nonManagedPaths.iterator(), managedPaths.iterator())
}

class JoinIterator<T>(private val first: Iterator<T>, private val second: Iterator<T>) : Iterator<T> {
    private var shouldUseFirst = true
    override fun hasNext(): Boolean {
        if (shouldUseFirst) {
            if (first.hasNext()) return true
            shouldUseFirst = false
        }
        return second.hasNext()
    }
    override fun next(): T = if (shouldUseFirst) first.next() else second.next()
}