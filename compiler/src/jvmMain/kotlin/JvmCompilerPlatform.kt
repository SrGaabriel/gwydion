package me.gabriel.gwydion.compiler.jvm

import kotlinx.serialization.json.Json
import me.gabriel.gwydion.compiler.GwydionCompilerPlatform
import me.gabriel.gwydion.compiler.cli.CommandLine
import me.gabriel.gwydion.compiler.io.IoPlatform
import me.gabriel.gwydion.tools.GwydionLogger
import okio.FileSystem

class JvmCompilerPlatform(
    override val logger: GwydionLogger,
    override val cli: CommandLine
): GwydionCompilerPlatform {
    override val io: IoPlatform = Io

    override fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun exitProcess(status: Int): Nothing {
        kotlin.system.exitProcess(status)
    }

    companion object Io : IoPlatform {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

        override fun getJson(): Json = json

        override fun getFileSystem(): FileSystem = FileSystem.SYSTEM
    }
}