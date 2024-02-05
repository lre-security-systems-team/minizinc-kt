package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznData
import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznValue
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText

data class Model(
    private val _data: MutableMap<String, DznValue> = mutableMapOf(),
    private val _codeFragments: MutableList<String> = mutableListOf(),
    private val enumMap: MutableMap<String, Any> = mutableMapOf(),
    private val _includes: MutableList<Path> = mutableListOf(),
    private var checker: Boolean = false,
) {

    val codeFragments: List<String> get() = _codeFragments
    val data: Map<String, DznValue> get() = _data
    val includes: List<Path> get() = _includes

    constructor(files: List<Path>) : this() {
        for (file in files) addFile(file)
    }
    constructor(file: Path) : this(listOf(file))

    operator fun get(key: String): DznValue? = _data[key]

    operator fun set(key: String, value: DznValue) {
        if (key !in _data) {
            _data[key] = value
        } else {
            if (_data[key] != value)
                throw IllegalArgumentException(
                    """
                    The parameter $key cannot be assigned multiple values.
                    If your are changing the model, consider using the branch    
                    method before assignment.
                """.trimIndent()
                )
        }
    }

    fun addFile(file: Path, parseData: Boolean = false) {
        if (!parseData) {
            _includes.add(file)
            return
        }
        when (file.extension) {
            "json" -> {
                val data = DznData.fromJsonObject(loads<JsonObject>(file.readText()))
                for ((key, value) in data.entries) {
                    this[key] = value
                }
            }
            in listOf("dzn", "mzn", "mzc") -> {
                // TODO: add parser dzn instructions
                if (file.extension == "mzc") {
                    checker = true
                }
                _includes.add(file)
            }

            else -> {
                throw IllegalArgumentException("Unknown file suffix ${file.extension}")
            }
        }
    }

    fun addString(fragment: String) {
        _codeFragments += fragment
    }

}