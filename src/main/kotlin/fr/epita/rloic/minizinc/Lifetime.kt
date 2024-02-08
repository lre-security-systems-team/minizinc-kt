package fr.epita.rloic.fr.epita.rloic.minizinc

import java.io.Closeable

/**
 * The Lifetime class is designed to clear some stuff after executing a function.
 * It is mainly used to clear temporary file when they are not used anymore.
 */
class Lifetime {

    companion object {
        inline operator fun <R> invoke(block: (Lifetime) -> R): R {
            val lifetime = Lifetime()
            var exception: Throwable? = null
            try {
                return block(lifetime)
            } catch (e: Throwable) {
                exception = e
                throw e
            } finally {
                lifetime.closeFinally(exception)
            }
        }
    }

    private class Foreach<T>(private val fn: (T) -> Unit): (Iterable<T>) -> Unit {
        override fun invoke(arg: Iterable<T>) {
            arg.forEach(fn)
        }
    }

    private class SubjectAction<T>(
        val receiver: T,
        val action: (T) -> Unit
    ) {
        fun run() {
            action(receiver)
        }
    }

    private val elements = mutableListOf<SubjectAction<*>>()

    fun <T: Closeable> closeOnExit(element: T): T {
        elements.add(SubjectAction(element, Closeable::close))
        return element
    }

    @JvmName("closeListOnExit")
    fun <T: Closeable> closeOnExit(elements: List<T>) {
        this.elements.add(SubjectAction(elements, Foreach(Closeable::close)))
    }

    fun <T: AutoCloseable> closeOnExit(element: T): T {
        elements.add(SubjectAction(element, AutoCloseable::close))
        return element
    }

    @JvmName("autoCloseListOnExit")
    fun <T: AutoCloseable> closeOnExit(elements: List<T>) {
        this.elements.add(SubjectAction(elements, Foreach(AutoCloseable::close)))
    }

    fun <T> runOnExit(element: T, fn: (T) -> Unit): T {
        elements.add(SubjectAction(element, fn))
        return element
    }

    @JvmName("runListOnExit")
    fun <T> runOnExit(elements: List<T>, fn: (T) -> Unit): List<T> {
        this.elements.add(SubjectAction(elements, Foreach(fn)))
        return elements
    }

    fun takeOwnership(element: Any, other: Lifetime) {
        val foundElement = other.elements.find { it.receiver == element }
        if (foundElement != null) {
            elements.add(foundElement)
            other.elements.remove(element)
        }
    }

    fun closeFinally(cause: Throwable?) = when {
        cause == null -> elements.forEach(SubjectAction<*>::run)
        else ->
            try {
                elements.forEach(SubjectAction<*>::run)
            } catch (closeException: Throwable) {
                cause.addSuppressed(closeException)
            }
    }
}