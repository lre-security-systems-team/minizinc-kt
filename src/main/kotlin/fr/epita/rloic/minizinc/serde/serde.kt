package fr.epita.rloic.fr.epita.rloic.minizinc.serde

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.internal.readJson
import kotlinx.serialization.serializer


val jsonSerde = Json
val jsonSerdeIgnoreUnknownKey = Json {
    ignoreUnknownKeys = true
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T> loads(json: JsonElement, ignoreUnknownKeys: Boolean = false) =
    (if (ignoreUnknownKeys) jsonSerdeIgnoreUnknownKey else jsonSerde)
        .readJson(json, Json.serializersModule.serializer<T>())

inline fun <reified T> loads(text: String) = jsonSerde.decodeFromString<T>(text)
inline fun <reified T> loads(text: String, ignoreUnknownKeys: Boolean) =
    (if (ignoreUnknownKeys) jsonSerdeIgnoreUnknownKey else jsonSerde).decodeFromString<T>(text)

inline fun <reified T> dumps(value: T) = jsonSerde.encodeToString(value)