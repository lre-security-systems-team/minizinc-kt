package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.mzn.Method
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class Statistics(
    val method: Method? = null,
    val paths: Long? = null,
    val nodes: Long? = null,
    val failures: Long? = null,
    val restarts: Long? = null,
    val variables: Long? = null,
    val intVars: Long? = null,
    val boolVariables: Long? = null,
    val propagators: Long? = null,
    val propagations: Long? = null,
    val peakDepth: Long? = null,
    val nogoods: Long? = null,
    val backjumps: Long? = null,
    val peakMem: Double? = null,
    var time: Double? = null,
    val initTime: Double? = null,
    val solveTime: Double? = null,
    val flatTime: Double? = null,
    val flatIntVars: Long? = null,
    val objective: JsonPrimitive? = null,
    val objectiveBound: JsonPrimitive? = null,
    val baseMem: Double? = null,
    val randomSeed: Long? = null,
    val nSolutions: Long? = null
) {
    fun update(other: Statistics) {}
}