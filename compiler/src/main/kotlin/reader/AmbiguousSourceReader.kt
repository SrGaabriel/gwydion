package me.gabriel.gwydion.compiler.reader

import me.gabriel.gwydion.reader.SourceReader
import me.gabriel.gwydion.tools.GwydionLogger
import me.gabriel.gwydion.tools.LogLevel
import java.io.File

// This is a shitty source reader that will work as a placeholder for the real one
class AmbiguousSourceReader(
    private val logger: GwydionLogger
): SourceReader {
    fun read(directory: File): String {
        logger.log(LogLevel.DEBUG) { +"Reading the directory: ${directory.absolutePath}" }
        val builder = StringBuilder()
        directory.listFiles()?.forEach {
            if (it.extension != "wy") return@forEach
            logger.log(LogLevel.DEBUG) { +"Reading the file: ${it.name}" }
            builder.appendLine(it.readText())
        }
        return builder.toString()
    }
}