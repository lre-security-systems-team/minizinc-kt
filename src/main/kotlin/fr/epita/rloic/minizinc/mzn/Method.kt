package fr.epita.rloic.fr.epita.rloic.minizinc.mzn

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Method.Serializer::class)
enum class Method(private val jsonValue: String) {
    SATISFY("sat"), MINIMIZE("min"), MAXIMIZE("max");

    object Serializer : KSerializer<Method> {
        override val descriptor = PrimitiveSerialDescriptor("Method", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: Method) {
            encoder.encodeString(value.jsonValue)
        }
        override fun deserialize(decoder: Decoder) = fromJsonValue(decoder.decodeString())
    }

    companion object {
        fun fromJsonValue(s: String): Method {
            return when (s) {
                "sat", "satisfy" -> SATISFY
                "min", "minimize" -> MINIMIZE
                "max", "maximize" -> MAXIMIZE
                else -> throw IllegalArgumentException("Unknown method $s, valid options are 'sat', 'min' and 'max'")
            }
        }
    }
}