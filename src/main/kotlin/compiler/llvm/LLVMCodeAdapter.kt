package me.gabriel.gwydion.compiler.llvm

import me.gabriel.gwydion.compiler.CodeGenerator
import me.gabriel.gwydion.compiler.ProgramMemoryRepository
import me.gabriel.gwydion.intrinsics.IntrinsicFunction
import me.gabriel.gwydion.parsing.SyntaxTree
import me.gabriel.gwydion.signature.Signatures
import java.io.File

class LLVMCodeAdapter: CodeGenerator {
    private val intrinsics = mutableListOf<IntrinsicFunction>()
    private val stdlibDependencies = mutableListOf<String>()

    override fun generate(
        module: String,
        tree: SyntaxTree,
        memory: ProgramMemoryRepository,
        signatures: Signatures,
        compileIntrinsics: Boolean
    ): String {
        val process = LLVMCodeAdaptationProcess(module, intrinsics, signatures, stdlibDependencies, compileIntrinsics)
        process.acceptNode(memory.root, tree.root)
        process.setup()
        process.finish()
        return process.getGeneratedCode()
    }

    override fun addStdlibDependency(dependency: String) {
        stdlibDependencies.add(dependency)
    }

    override fun registerIntrinsicFunction(vararg functions: IntrinsicFunction) {
        intrinsics.addAll(functions)
    }

    fun generateExecutable(llvmIr: String, outputDir: String, outputFileName: String) {
        val outputDirectory = File(outputDir)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        val inputLlPath = "$outputDir/$outputFileName.ll"
//        val outputExePath = "$outputDir/$outputFileName"

//        File(outputExePath).delete()
        File(inputLlPath).delete()
        File(inputLlPath).createNewFile()
        File(inputLlPath).writeText(llvmIr)

//        val clangProcess = ProcessBuilder(
//            "clang",
//            inputLlPath,
//            "-o",
//            outputExePath
//        )
//            .redirectError(ProcessBuilder.Redirect.INHERIT)
//            .start()
//        clangProcess.waitFor()
    }
}