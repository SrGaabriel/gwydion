package me.gabriel.gwydion.compiler.llvm

import me.gabriel.gwydion.llvm.struct.LLVMType
import me.gabriel.gwydion.llvm.struct.extractPrimitiveType
import me.gabriel.gwydion.parsing.Type

fun Type.asLLVM(): LLVMType = when (this) {
    Type.String -> LLVMType.Pointer(LLVMType.I8)
    Type.Void -> LLVMType.Void
    Type.Any -> LLVMType.I32
    Type.Int32 -> LLVMType.I32
    Type.Boolean -> LLVMType.I1
    is Type.FixedArray -> LLVMType.Array(
        type = this.type.asLLVM(),
        length = this.length
    )
    is Type.DynamicArray -> LLVMType.Pointer(this.type.asLLVM())
    is Type.Struct -> LLVMType.Struct(
        name = this.identifier,
        fields = this.fields.mapValues { it.value.asLLVM() }
    )
    else -> error("Unsupported LLVM type $this")
}