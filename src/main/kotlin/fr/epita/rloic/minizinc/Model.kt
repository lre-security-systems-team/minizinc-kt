package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznData
import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznValue
import fr.epita.rloic.fr.epita.rloic.minizinc.serde.loads
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText

private fun <T> identity(element: T): T = element

class Model<T>(
    val outputType: (JsonObject) -> T
) {

    companion object {
        operator fun invoke(): Model<JsonObject> = Model(::identity)

        operator fun invoke(path: Path): Model<JsonObject> = Model(path, ::identity)

        operator fun invoke(paths: List<Path>): Model<JsonObject> = Model(paths, ::identity)

    }

    private val _data: MutableMap<String, DznValue> = mutableMapOf()
    private val _codeFragments: MutableList<String> = mutableListOf()
    private val enumMap: MutableMap<String, Any> = mutableMapOf()
    private val _includes: MutableList<Path> = mutableListOf()
    private var checker: Boolean = false

    constructor(path: Path, outputType: (JsonObject) -> T) : this(outputType) {
        addFile(path)
    }

    constructor(paths: List<Path>, outputType: (JsonObject) -> T) : this(outputType) {
        for (path in paths) {
            addFile(path)
        }
    }

    val codeFragments: List<String> get() = _codeFragments
    val data: Map<String, DznValue> get() = _data
    val includes: List<Path> get() = _includes


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

    operator fun set(key: String, value: String): Unit = set(key, DznValue.Str(value))
    operator fun set(key: String, value: Number): Unit = set(key, DznValue.Num(value))
    operator fun set(key: String, value: List<DznValue>): Unit = set(key, DznValue.Arr(value))
    operator fun set(key: String, value: Set<DznValue>): Unit = set(key, DznValue.Set(value.toList()))

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