package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.mzn.Method
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


typealias MilliSeconds = Double

@Serializable
data class Statistics(
    var method: Method? = null,
    var paths: Long? = null,
    var nodes: Long? = null,
    var failures: Long? = null,
    var restarts: Long? = null,
    var variables: Long? = null,
    var intVars: Long? = null,
    var boolVariables: Long? = null,
    var propagators: Long? = null,
    var propagations: Long? = null,
    var peakDepth: Long? = null,
    var nogoods: Long? = null,
    var backjumps: Long? = null,
    var peakMem: Double? = null,
    var time: MilliSeconds? = null,
    var initTime: MilliSeconds? = null,
    var solveTime: MilliSeconds? = null,
    var flatTime: MilliSeconds? = null,
    var flatIntVars: Long? = null,
    var flatIntConstraints: Long? = null,
    var flatBoolVars: Long? = null,
    var flatBoolConstraints: Long? = null,
    var objective: JsonPrimitive? = null,
    var objectiveBound: JsonPrimitive? = null,
    var baseMem: Double? = null,
    var randomSeed: Long? = null,
    var nSolutions: Long? = null
) {
    fun update(other: Statistics) {
        for (field in other::class.memberProperties) {
            if (field is KMutableProperty1<*, *>) {
                if (field is KMutableProperty1) {
                    val typedField = (field as KMutableProperty1<Statistics, Any?>)
                    val value = typedField.get(other)
                    if (value != null) {
                        typedField.set(this, value)
                    }
                }
            }
        }
    }

    override fun toString() = buildString {
        for (field in this@Statistics::class.memberProperties) {
            val value = (field as KProperty1<Statistics, Any?>).get(this@Statistics)
            if (value != null) {
                if (isNotEmpty()) append(", ")
                append(field.name)
                append(": ")
                append(value)
            }
        }
    }
}