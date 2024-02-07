package fr.epita.rloic

import fr.epita.rloic.fr.epita.rloic.minizinc.*
import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznData
import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznValue
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.dumps
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.jsonSerde
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import jdk.jshell.Diag
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Default.serializersModule
import kotlinx.serialization.json.internal.readJson
import kotlinx.serialization.serializer
import kotlin.io.path.Path

@Serializable
data class Problem(val x: List<Int>)

fun main() {
    val model = Model<Problem>(Path("test.mzn")) { loads(it, true) }

    val solver = Solver.lookup("picat")
    System.err.println("%% " + solver.name + " " + solver.version)

    val instance = Instance(solver, model)
    val (_, solution, stats) = instance.solve(
        nrSolutions = 5
    )

    println(stats)
    solution?.forEach(::println)
}


