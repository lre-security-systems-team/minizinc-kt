package fr.epita.rloic.fr.epita.rloic.minizinc.mzn

import fr.epita.rloic.fr.epita.rloic.minizinc.Location
import fr.epita.rloic.fr.epita.rloic.minizinc.error.MznExecutionError
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.PathSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path
import fr.epita.rloic.fr.epita.rloic.minizinc.Statistics as _Statistics
import fr.epita.rloic.fr.epita.rloic.minizinc.Status as _Status

@Serializable
sealed class JsonOutput {

    @Serializable
    @SerialName("interface")
    data class Interface(
        val method: Method,
        val input: JsonObject, // TODO: refine type
        val output: JsonObject, // TODO: refine type
        @SerialName("has_output_item")
        val hasOutputItem: Boolean,
        @SerialName("included_files")
        val includedFiles: List<@Serializable(with = PathSerializer::class) Path>,
        val globals: List<JsonObject>
    ) : JsonOutput()

    @Serializable
    @SerialName("error")
    data class Error(
        val what: String = "",
        val location: Location? = null,
        val message: String = "",
    ) : JsonOutput() {
        fun throws(): Nothing {
            throw MznExecutionError(this)
        }
    }

    @Serializable
    @SerialName("statistics")
    data class Statistics(val statistics: _Statistics) : JsonOutput()

    @Serializable
    @SerialName("comment")
    data class Comment(val comment: String) : JsonOutput()



    @Serializable
    @SerialName("solution")
    data class Solution(
        val output: Output,
        val time: Double,
        val sections: List<String>? = null
    ) : JsonOutput() {
        @Serializable
        data class Output(val json: JsonObject)
    }

    @Serializable
    @SerialName("status")
    data class Status(val status: _Status, val time: Double? = null) : JsonOutput()

    @Serializable
    @SerialName("time")
    data class Time(val time: Double) : JsonOutput()

    @Serializable
    @SerialName("checker")
    class Checker() : JsonOutput()

}