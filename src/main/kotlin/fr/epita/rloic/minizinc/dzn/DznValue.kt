package fr.epita.rloic.fr.epita.rloic.minizinc.dzn

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
sealed class DznValue {
    companion object {
        fun fromJsonElement(element: JsonElement): DznValue {
            return when (element) {
                is JsonPrimitive -> {
                    val bool = element.booleanOrNull
                    if (bool != null) return Bool(bool)
                    val number = element.intOrNull ?: element.longOrNull ?: element.floatOrNull ?: element.doubleOrNull
                    if (number != null) return Num(number)
                    Str(element.content)
                }

                is JsonArray -> {
                    Arr(element.stream().map(Companion::fromJsonElement).toList())
                }

                is JsonObject -> {
                    if (element.keys == setOf("e")) {
                        Enum((element["e"] as JsonPrimitive).content)
                    } else if (element.keys == setOf("c", "e")) {
                        ConstrEnum(
                            (element["c"] as JsonPrimitive).content,
                            (element["e"] as JsonPrimitive).content
                        )
                    } else if (element.keys == setOf("c", "i")) {
                        AnonEnum(
                            (element["e"] as JsonPrimitive).content,
                            (element["i"] as JsonPrimitive).content
                        )
                    } else if (element.keys == setOf("set") && element["set"] is JsonArray) {
                        Set((element["set"] as JsonArray).stream().map(Companion::fromJsonElement).toList())
                    } else {
                        throw IllegalArgumentException("Nested json objects are not allowed.")
                    }
                }
            }
        }
    }

    abstract fun toJsonElement(): JsonElement

    data class Str(val value: String) : DznValue() {
        override fun toJsonElement() = JsonPrimitive(value)
    }

    data class Num(val value: Number) : DznValue() {
        override fun toJsonElement() = JsonPrimitive(value)
    }

    data class Bool(val value: Boolean) : DznValue() {
        override fun toJsonElement() = JsonPrimitive(value)
    }

    data class Arr(val values: List<DznValue>) : DznValue() {
        override fun toJsonElement() = JsonArray(values.map(DznValue::toJsonElement))
    }

    data class Set(val values: List<DznValue>) : DznValue() {
        override fun toJsonElement() = JsonObject(mapOf(
            "set" to JsonArray(values.map(DznValue::toJsonElement))
        ))
    }

    data class Enum(val e: String) : DznValue() {
        override fun toJsonElement() = JsonObject(mapOf(
            "e" to JsonPrimitive(e)
        ))
    }

    data class AnonEnum(val e: String, val i: String) : DznValue() {
        override fun toJsonElement() = JsonObject(mapOf(
            "e" to JsonPrimitive(e),
            "i" to JsonPrimitive(i)
        ))
    }

    data class ConstrEnum(val c: String, val e: String) : DznValue() {
        override fun toJsonElement() = JsonObject(mapOf(
            "c" to JsonPrimitive(c),
            "e" to JsonPrimitive(e)
        ))
    }
}