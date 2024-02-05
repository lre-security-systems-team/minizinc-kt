package fr.epita.rloic.fr.epita.rloic.minizinc

import kotlinx.serialization.Serializable

@Serializable
data class Statistics(
    val nodes: Int = 0,
    val failures: Int = 0,
    val restarts: Int = 0,
    val variables: Int = 0,
    val intVars: Int = 0,
    val boolVariables: Int = 0,
    val propagators: Int = 0,
    val propagations: Int = 0,
    val peakDepth: Int = 0,
    val nogoods: Int = 0,
    val backjumps: Int = 0,
    val peakMem: Double = 0.0,
    var time: Double = 0.0,
    val initTime: Double = 0.0,
    val solveTime: Double = 0.0,
    val baseMem: Double = 0.0,
    val randomSeed: Long = 0
) {
    fun update(other: Statistics) {}
}