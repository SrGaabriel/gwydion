package me.gabriel.selene.llvm

import me.gabriel.selene.llvm.struct.BinaryOp
import me.gabriel.selene.llvm.struct.LLVMType
import me.gabriel.selene.llvm.struct.MemoryUnit
import me.gabriel.selene.llvm.struct.Value

interface ILLVMCodeAssembler {
    fun addDependency(dependency: String)

    fun add(instruction: String)

    fun instruct(instruction: String)

    fun allocateStackMemory(type: LLVMType, alignment: Int): MemoryUnit

    fun allocateHeapMemory(size: Int): MemoryUnit

    fun allocateHeapMemoryAndCast(
        size: Int,
        type: LLVMType
    ): MemoryUnit

    fun declareFunction(name: String, returnType: LLVMType, arguments: List<MemoryUnit>)

    fun createBranch(label: String)

    fun conditionalBranch(condition: MemoryUnit, trueLabel: String, falseLabel: String)

    fun compareAndBranch(
        condition: Value,
        trueLabel: String,
        falseLabel: String
    )

    fun unconditionalBranchTo(label: String)

    fun createArray(
        type: LLVMType,
        size: Int?,
        elements: List<Value>
    ): MemoryUnit

    fun getElementFromStructure(
        struct: Value,
        type: LLVMType,
        index: Value,
        total: Boolean = true
    ): MemoryUnit

    fun getElementFromVirtualTable(
        table: String,
        tableType: LLVMType,
        type: LLVMType,
        index: Value,
        total: Boolean = true
    ): MemoryUnit

    fun loadPointer(value: MemoryUnit): MemoryUnit

    fun unsafelyLoadPointer(value: MemoryUnit, pointer: LLVMType.Pointer): MemoryUnit

    fun setStructElementTo(
        value: Value,
        struct: Value,
        type: LLVMType,
        index: Value
    ): MemoryUnit

    fun closeBrace()

    fun functionDsl(
        name: String,
        returnType: LLVMType,
        arguments: List<MemoryUnit>,
        callback: (ILLVMCodeAssembler) -> Unit
    ) {
        declareFunction(
            name, returnType, arguments
        )
        callback(this)
        closeBrace()
    }

    fun returnVoid()

    fun returnValue(value: Value)

    fun callFunction(
        name: String,
        arguments: Collection<Value>,
        assignment: Value,
        local: Boolean = false
    )

    fun declareStruct(
        name: String,
        fields: Map<String, LLVMType>
    ): MemoryUnit

    fun createVirtualTable(
        name: String,
        functions: List<LLVMType.Function>
    ): MemoryUnit

    fun addNumber(
        type: LLVMType,
        left: Value,
        right: Value
    ): MemoryUnit

    fun binaryOp(
        type: LLVMType,
        left: Value,
        op: BinaryOp,
        right: Value
    ): MemoryUnit

    fun simpleString(text: String): Value

    fun buildString(text: String): MemoryUnit

    fun storeTo(value: Value, address: MemoryUnit)

    fun returnValue(type: LLVMType, value: Value)

    fun copySourceToDestinationString(source: MemoryUnit, destination: MemoryUnit)

    fun addSourceToDestinationString(source: MemoryUnit, destination: MemoryUnit)

    fun calculateStringLength(string: MemoryUnit): MemoryUnit

    fun handleComparison(left: Value, right: Value, type: LLVMType): MemoryUnit

    fun compareStrings(left: Value, right: Value): MemoryUnit

    fun isTrue(value: Value): MemoryUnit

    fun isFalse(value: Value): MemoryUnit

    fun nextRegister(): Int

    fun nextLabel(prefix: String = "label"): String

    fun finish(): String
}