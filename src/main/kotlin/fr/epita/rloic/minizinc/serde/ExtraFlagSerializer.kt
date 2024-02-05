package fr.epita.rloic.fr.epita.rloic.minizinc.serde

import fr.epita.rloic.fr.epita.rloic.minizinc.ExtraFlag
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ExtraFlagSerializer: KSerializer<ExtraFlag> {

    private val delegateSerializer = ListSerializer(String.serializer())

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("ExtraFlag", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: ExtraFlag) {
        val data = listOf(
            value.name,
            value.description,
            value.type,
            value.defaultValue
        )
        encoder.encodeSerializableValue(delegateSerializer, data)
    }

    override fun deserialize(decoder: Decoder): ExtraFlag {
        val list = decoder.decodeSerializableValue(delegateSerializer)
        return ExtraFlag(
            list[0],
            list[1],
            list[2],
            list[3]
        )
    }


}