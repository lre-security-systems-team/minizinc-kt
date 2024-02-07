package fr.epita.rloic.fr.epita.rloic.minizinc.utils

class IterMap<I, O, F>(private val iter: Iterator<I>, private val fn: F): Iterator<O> where F: (I) -> O {
    override fun hasNext() = iter.hasNext()
    override fun next(): O = fn(iter.next())
}