package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.utils.IterMap

sealed class Solution<T>: Iterable<T> {
    data class Single<T>(val data: T) : Solution<T>() {
        class SingleElementIterator<T>(private val element: T): Iterator<T> {
            private var consumed = false
            override fun hasNext(): Boolean {
                if (consumed) return false
                consumed = true
                return true
            }
            override fun next() = if (consumed) element else throw NoSuchElementException()
        }
        override fun iterator(): Iterator<T> = SingleElementIterator(data)
    }
    data class MultipleSolutions<T>(val solutions: MutableList<Single<T>> = mutableListOf()) : Solution<T>() {
        operator fun plusAssign(other: Solution<T>) {
            when (other) {
                is Single -> solutions += other
                is MultipleSolutions -> solutions += other.solutions
            }
        }
        override fun iterator() = IterMap(solutions.iterator(), Single<T>::data)
    }
}

