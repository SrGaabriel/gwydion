package me.gabriel.selene.llvm

import me.gabriel.selene.llvm.struct.*

class LLVMCodeAssembler(val generator: ILLVMCodeGenerator): ILLVMCodeAssembler {
    private val ir = mutableListOf<String>()
    private var register = 0
    private var label = 0
//    var cursor = 0

    override fun addDependency(dependency: String) {
        ir.add(0, dependency)
    }

    override fun finish(): String = ir.joinToString("\n")

    override fun add(instruction: String) {
        ir.add(instruction)
    }

    override fun instruct(instruction: String) {
        ir.add("    $instruction")
    }

    fun saveToRegister(register: Int, expression: String) {
        instruct("%$register = $expression")
    }

    override fun allocateStackMemory(type: LLVMType, alignment: Int): MemoryUnit {
        val register = nextRegister()
        val pointer = LLVMType.Pointer(type)
        saveToRegister(register, generator.stackMemoryAllocation(pointer, alignment))
        return MemoryUnit.Sized(register, pointer, type.size)
    }

    override fun allocateHeapMemory(size: Int): MemoryUnit {
        val register = nextRegister()
        saveToRegister(register, generator.heapMemoryAllocation(LLVMType.I8, size))
        val value = MemoryUnit.Sized(register, LLVMType.Pointer(LLVMType.I8), size)
        instruct(generator.heapMemoryDefinition(size, value))
        return value
    }

    override fun allocateHeapMemoryAndCast(size: Int, type: LLVMType): MemoryUnit {
        val value = allocateHeapMemory(size)
        val cast = MemoryUnit.Sized(
            register = nextRegister(),
            type = type,
            size = type.size
        )
        saveToRegister(cast.register, generator.cast(value, type))
        return cast
    }

    override fun declareFunction(name: String, returnType: LLVMType, arguments: List<MemoryUnit>) {
        add(generator.functionDeclaration(name, returnType, arguments))
    }

    override fun createBranch(label: String) {
        add(generator.createBranch(label))
    }

    override fun conditionalBranch(condition: MemoryUnit, trueLabel: String, falseLabel: String) {
        instruct(generator.conditionalBranch(condition, trueLabel, falseLabel))
    }

    override fun compareAndBranch(condition: Value, trueLabel: String, falseLabel: String) {
        if (condition.type !== LLVMType.I1) {
            error("Condition must be of type i1")
        }
        val comparison = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.I1,
            size = 1
        )
        saveToRegister(
            comparison.register,
            generator.signedIntegerComparison(condition, LLVMConstant(1, LLVMType.I1))
        )
        instruct(generator.conditionalBranch(comparison, trueLabel, falseLabel))
    }

    override fun unconditionalBranchTo(label: String) {
        register++
        instruct(generator.unconditionalBranchTo(label))
    }

    override fun closeBrace() {
        add("}")
    }

    override fun addNumber(type: LLVMType, left: Value, right: Value): MemoryUnit =
        binaryOp(type, left, BinaryOp.Addition, right)

    override fun binaryOp(type: LLVMType, left: Value, op: BinaryOp, right: Value): MemoryUnit {
        val register = nextRegister()
        saveToRegister(register, generator.binaryOp(left, op, right, type))
        return MemoryUnit.Sized(
            register = register,
            type = type,
            size = type.size
        )
    }

//    override fun dynamicMemoryUnitAllocation(unit: MemoryUnit) {
//        when (unit) {
//            is MemoryUnit.Unsized -> saveToRegister(unit.register, generator.heapMemoryAllocation(
//                size = min(unit.type.size, 64),
//                type = unit.type
//            ))
//            is MemoryUnit.Sized -> {
//                if (unit.size > 1024) {
//                    saveToRegister(unit.register, generator.heapMemoryAllocation(
//                        size = unit.size,
//                        type = unit.type
//                    ))
//                }
//                saveToRegister(unit.register, generator.stackMemoryAllocation(
//                    type = unit.type,
//                    alignment = unit.type.defaultAlignment
//                ))
//            }
//            is MemoryUnit.TraitData -> {
//                error("Trait data cannot be allocated dynamically")
//            }
//            NullMemoryUnit -> error("Tried to store null memory unit")
//        }
//    }

    override fun callFunction(name: String, arguments: Collection<Value>, assignment: Value, local: Boolean) {
        val call = generator.functionCall(
            name = name,
            arguments = arguments,
            returnType = assignment.type,
            local = local
        )

        when (assignment) {
            NullMemoryUnit -> instruct(call).also { register++ }
            is MemoryUnit -> saveToRegister(assignment.register, call)
            else -> instruct(call)
        }
    }

    override fun declareStruct(name: String, fields: Map<String, LLVMType>): MemoryUnit {
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.Struct(name, fields),
            size = fields.values.sumOf { it.size }
        )
        addDependency("%$name = ${generator.structDeclaration(fields.values)}")
        return unit
    }

    override fun createVirtualTable(name: String, functions: List<LLVMType.Function>): MemoryUnit {
        val unit = MemoryUnit.Unsized(
            register = nextRegister(),
            type = LLVMType.Trait(name, functions.size)
        )
        instruct("%$name = ${generator.virtualTableDeclaration(name, functions)}")
        return unit
    }

    override fun createArray(type: LLVMType, size: Int?, elements: List<Value>): MemoryUnit {
        // TODO: improve code
        val unit = if (size != null) {
            this.allocateStackMemory(
                type = LLVMType.Array(type, size),
                alignment = type.defaultAlignment
            )
        } else {
            this.allocateHeapMemoryAndCast(
                size = 64,
                type = LLVMType.Pointer(type)
            )
        }

        var firstPointer: MemoryUnit? = null
        elements.forEachIndexed { index, element ->
            val reference = setStructElementTo(
                value = element,
                struct = unit,
                type = type,
                index = LLVMConstant(value = index, type = LLVMType.I32)
            )
            if (firstPointer == null) {
                firstPointer = reference
            }
        }
//        if (firstPointer == null) {
//            return unit
//        }

        return unit
    }

    /**
     * @param struct The struct to get the element from
     * @param type The type of the returned element
     * @param index The index of the element to get
     * @param total Whether to read from start of struct or from the pointer
     */
    override fun getElementFromStructure(
        struct: Value,
        type: LLVMType,
        index: Value,
        total: Boolean
    ): MemoryUnit {
        val reading = if (total) {
            generator.unsafeSubElementAddressTotalReading(
                struct = struct,
                index = index
            )
        } else {
            generator.unsafeSubElementAddressDirectReading(
                struct = struct,
                index = index
            )
        }
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.Pointer(type),
            size = type.size
        )
        saveToRegister(
            register = unit.register,
            expression = reading
        )
        return unit
    }

    override fun getElementFromVirtualTable(
        table: String,
        tableType: LLVMType,
        type: LLVMType,
        index: Value,
        total: Boolean
    ): MemoryUnit {
        val reading = generator.virtualTableReading(
            table = table,
            tableType = tableType,
            index = index
        )
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.Pointer(type),
            size = type.size
        )
        saveToRegister(
            register = unit.register,
            expression = reading
        )
        return unit
    }

    override fun loadPointer(value: MemoryUnit): MemoryUnit {
        if (value.type == LLVMType.Ptr) {
            val unit = MemoryUnit.Sized(
                register = nextRegister(),
                type = value.type,
                size = value.type.size
            )
            saveToRegister(
                register = unit.register,
                expression = generator.loadPointer(value.type, value)
            )
            return unit
        }

        if (value.type !is LLVMType.Pointer) {
            error("Expected pointer type, got ${value.type}")
        }
        val pointerType = value.type as LLVMType.Pointer
        return unsafelyLoadPointer(value, pointerType)
    }

    override fun unsafelyLoadPointer(value: MemoryUnit, pointer: LLVMType.Pointer): MemoryUnit {
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = pointer.type,
            size = pointer.size
        )
        saveToRegister(
            register = unit.register,
            expression = generator.loadPointer(pointer.type, value)
        )
        return unit
    }

    override fun setStructElementTo(
        value: Value,
        struct: Value,
        type: LLVMType,
        index: Value
    ): MemoryUnit {
        val reference = getElementFromStructure(
            struct, type, index, (struct.type as? LLVMType.Pointer)?.type !is LLVMType.Pointer
        )
        instruct(generator.storage(value, reference))
        return reference
    }

    override fun storeTo(value: Value, address: MemoryUnit) {
        instruct(generator.storage(value, address))
    }

    override fun returnValue(type: LLVMType, value: Value) {
        instruct(generator.returnInstruction(type, value))
    }

    override fun simpleString(text: String): Value {
        return LLVMConstant(
            value = "c\"$text\\00\"",
            type = LLVMType.Array(LLVMType.I8, text.length + 1)
        )
    }

    override fun buildString(text: String): MemoryUnit =
        createArray(
            type = LLVMType.I8,
            size = text.length + 1,
            elements = text.map {
                LLVMConstant(it.code, LLVMType.I8)
            } + LLVMConstant(0, LLVMType.I8)
        )

    override fun returnVoid() {
        instruct("ret void")
    }

    override fun returnValue(value: Value) {
        instruct("ret ${value.type.llvm} ${value.llvm()}")
    }

    override fun copySourceToDestinationString(source: MemoryUnit, destination: MemoryUnit) {
        nextRegister()
        instruct(generator.stringCopy(source, destination))
    }

    override fun addSourceToDestinationString(source: MemoryUnit, destination: MemoryUnit) {
        nextRegister()
        instruct(generator.concatenateStrings(source, destination))
    }

    override fun calculateStringLength(string: MemoryUnit): MemoryUnit {
        val register = nextRegister()
        saveToRegister(register, generator.stringLengthCalculation(string))
        return MemoryUnit.Sized(register, LLVMType.I32, 8)
    }

    override fun isTrue(value: Value): MemoryUnit =
        isBooleanValue(value, expected = true)

    override fun isFalse(value: Value): MemoryUnit =
        isBooleanValue(value, expected = false)

    fun isBooleanValue(value: Value, expected: Boolean): MemoryUnit =
        booleanComparison(value, LLVMConstant(if (expected) 1 else 0, LLVMType.I1))

    fun booleanComparison(value: Value, expected: Value): MemoryUnit {
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.I1,
            size = 1
        )
        saveToRegister(
            register = unit.register,
            expression = generator.signedIntegerComparison(value, expected)
        )
        return unit
    }

    fun customComparison(comparison: Comparison): MemoryUnit {
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.I1,
            size = 1
        )
        saveToRegister(
            register = unit.register,
            expression = generator.comparison(comparison)
        )
        return unit
    }

    fun booleanInverseComparison(value: Value, expected: Value): MemoryUnit {
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.I1,
            size = 1
        )
        saveToRegister(
            register = unit.register,
            expression = generator.signedIntegerNotEqualComparison(value, expected)
        )
        return unit
    }

    override fun compareStrings(left: Value, right: Value): MemoryUnit {
        val unit = MemoryUnit.Sized(
            register = nextRegister(),
            type = LLVMType.I1,
            size = 1
        )
        saveToRegister(
            register = unit.register,
            expression = generator.stringComparison(left, right)
        )
        return unit
    }

    override fun handleComparison(left: Value, right: Value, type: LLVMType): MemoryUnit {
        when (type) {
            LLVMType.I1 -> {
                val comparison = addNumber(
                    type = type,
                    left = left,
                    right = right
                )
                return isTrue(comparison)
            }
            LLVMType.I8, LLVMType.I16, LLVMType.I32, LLVMType.I64 -> {
                return booleanComparison(left, right)
            }
            is LLVMType.Pointer -> {
                if (type.type == LLVMType.I8) {
                    val comparison = compareStrings(
                        left = left,
                        right = right
                    )
                    return isFalse(comparison)
                }
                error("Type ${type.type} not supported")
            }
            else -> error("Type $type not supported")
        }
    }

    override fun nextRegister(): Int = register++

    override fun nextLabel(prefix: String): String = "$prefix${label++}"
}