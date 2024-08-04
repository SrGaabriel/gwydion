package me.gabriel.gwydion;

import me.gabriel.gwydion.compiler.ProgramMemoryRepository
import me.gabriel.gwydion.compiler.llvm.LLVMCodeGenerator
import me.gabriel.gwydion.executor.KotlinCodeExecutor
import me.gabriel.gwydion.executor.PrintFunction
import me.gabriel.gwydion.executor.PrintlnFunction
import me.gabriel.gwydion.log.LogLevel
import me.gabriel.gwydion.log.MordantLogger
import java.io.File
import java.time.Instant

fun main() {
    val logger = MordantLogger()
    logger.log(LogLevel.INFO) { +"Starting the Gwydion compiler..." }

    val memoryStart = Instant.now()
    val example2 = File("src/main/resources/example2.wy").readText()
    val memory = ProgramMemoryRepository()
    val tree = parse(logger, example2, memory) ?: return
    val llvmCodeGenerator = LLVMCodeGenerator()
    llvmCodeGenerator.registerIntrinsicFunction(
        PrintFunction(),
        PrintlnFunction()
    )
    val memoryEnd = Instant.now()
    val memoryDelay = memoryEnd.toEpochMilli() - memoryStart.toEpochMilli()
    logger.log(LogLevel.INFO) { +"Memory analysis took ${memoryDelay}ms" }

    val generationStart = Instant.now()
    val generated = llvmCodeGenerator.generate(tree, memory)
    val generationEnd = Instant.now()
    val generationDelay = generationEnd.toEpochMilli() - generationStart.toEpochMilli()
    logger.log(LogLevel.INFO) { +"Code generation took ${generationDelay}ms" }
    println(generated)
    val compilingStart = Instant.now()
    llvmCodeGenerator.generateExecutable(
        llvmIr = generated,
        outputDir = "xscales",
        outputFileName = "output.exe"
    )
    val executionEnd = Instant.now()
    val executionDelay = executionEnd.toEpochMilli() - compilingStart.toEpochMilli()
    logger.log(LogLevel.INFO) { +"Compiling took ${executionDelay}ms" }

    logger.log(LogLevel.INFO) { +"The total time to generate and compile the code was ${executionDelay + generationDelay + memoryDelay}ms" }
    return
}

fun readText(): File {
    return File("src/main/resources")
}

fun findStdlib(): File {
    return File("stdlib/src/")
}