package fr.epita.rloic.fr.epita.rloic.minizinc.extensions

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

fun Path.expandsUser(): Path {
    if (startsWith("~" + File.separator))
        return Path(System.getProperty("user.home") + toString().substring(1))
    return this
}