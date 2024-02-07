package fr.epita.rloic.fr.epita.rloic.minizinc.dzn

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

data class DznData(private val delegate: Map<String, DznValue>): Map<String, DznValue> by delegate {
    companion object {
        fun fromJsonObject(root: JsonObject): DznData {
            val values = mutableMapOf<String, DznValue>()
            for ((key, value) in root.entries) {
                values[key] = DznValue.fromJsonElement(value)
            }
            return DznData(values)
        }
    }

    fun toJsonObject(): JsonObject {
        val jsonValues = mutableMapOf<String, JsonElement>()
        for ((key, value) in delegate) {
            jsonValues[key] = value.toJsonElement()
        }
        return JsonObject(jsonValues)
    }

    override fun toString() = delegate.toString()

}