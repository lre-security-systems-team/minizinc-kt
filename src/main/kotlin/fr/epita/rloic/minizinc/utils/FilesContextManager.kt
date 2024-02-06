package fr.epita.rloic.fr.epita.rloic.minizinc.utils

import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class FilesContextManager(
    private val nonManagedPaths: List<Path>,
    private val managedPaths: List<Path>
) : AutoCloseable, Iterable<Path> {
    override fun close() {
        managedPaths.forEach(Path::deleteIfExists)
    }
    override fun iterator(): Iterator<Path> = JoinIterator(nonManagedPaths.iterator(), managedPaths.iterator())
}

