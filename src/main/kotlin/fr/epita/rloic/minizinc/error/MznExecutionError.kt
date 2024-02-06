package fr.epita.rloic.fr.epita.rloic.minizinc.error

import fr.epita.rloic.fr.epita.rloic.minizinc.mzn.JsonOutput

class MznExecutionError(error: JsonOutput.Error) : RuntimeException(
    '\n' +
            """ 
                what: ${error.what},
                location: ${error.location},
                message: ${error.message}
            """.trimIndent()
)