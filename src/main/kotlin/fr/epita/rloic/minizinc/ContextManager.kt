package fr.epita.rloic.fr.epita.rloic.minizinc

class ContextManager : AutoCloseable {

    private val elements = mutableListOf<AutoCloseable>()

    fun <T : AutoCloseable, U> use(autoCloseable: T, consumer: (T) -> U): U {
        elements.add(autoCloseable)
        try {
            return consumer(autoCloseable)
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    override fun close() {
        elements.forEach(AutoCloseable::close)
    }
}