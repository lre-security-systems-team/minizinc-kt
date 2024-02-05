package fr.epita.rloic.fr.epita.rloic.minizinc.serde

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val jsonSerde = Json {
    ignoreUnknownKeys = true
}


inline fun <reified T> loads(text: String) = jsonSerde.decodeFromString<T>(text)
inline fun <reified T> dumps(value: T) = jsonSerde.encodeToString(value)