package fr.epita.rloic.fr.epita.rloic.minizinc.utils

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