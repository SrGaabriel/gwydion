package me.gabriel.gwydion.analyzer

import me.gabriel.gwydion.compiler.MemoryBlock
import me.gabriel.gwydion.exception.AnalysisError
import me.gabriel.gwydion.parsing.*
import me.gabriel.gwydion.signature.SignatureFunction
import me.gabriel.gwydion.signature.SignatureTrait
import me.gabriel.gwydion.signature.SignatureTraitImpl
import me.gabriel.gwydion.signature.Signatures
import me.gabriel.gwydion.util.Either

tailrec fun getExpressionType(
    block: MemoryBlock,
    node: SyntaxTreeNode,
    signatures: Signatures
): Either<AnalysisError, out Type> {
    return when (node) {
        is VariableReferenceNode -> {
            if (node.name == "self") {
                return Either.Right(block.self ?: return Either.Left(AnalysisError.UndefinedVariable(node, node.name, block)))
            }
            Either.Right(block.figureOutSymbol(node.name) ?: return Either.Left(AnalysisError.UndefinedVariable(node, node.name, block)))
        }
        is AssignmentNode -> {
            if (node.type == Type.Unknown) {
                getExpressionType(block, node.expression, signatures).getRightOrNull() ?: Type.Unknown
            } else {
                node.type
            }

            getExpressionType(block, node.expression, signatures)
        }
        is TypedSyntaxTreeNode -> Either.Right(node.type)
        is BinaryOperatorNode -> getExpressionType(block, node.left, signatures)
        is CallNode -> Either.Right(inferCallType(block, signatures, node))
        is EqualsNode -> Either.Right(Type.Boolean)
        is ArrayNode -> getExpressionType(block, node.elements.first(), signatures).let {
            it.mapRight {
                if (!node.dynamic) Type.FixedArray(it, node.elements.size)
                else Type.DynamicArray(it)
            }
        }
        is ArrayAccessNode -> {
            val arrayType = getExpressionType(block, node.array, signatures).getRightOrNull()
                ?: return Either.Left(AnalysisError.UndefinedArray(node, node.array.mark.value))
            when (arrayType) {
                is Type.FixedArray -> Either.Right(arrayType.baseType)
                is Type.DynamicArray -> Either.Right(arrayType.baseType)
                else -> Either.Left(AnalysisError.NotAnArray(node, arrayType))
            }
        }
        is InstantiationNode -> {
            val struct = block.figureOutSymbol(node.name) ?: return Either.Left(AnalysisError.UndefinedDataStructure(node, node.name))
            Either.Right(struct)
        }
        is DataStructureNode -> Either.Right(block.figureOutSymbol(node.name) ?: return Either.Left(AnalysisError.UndefinedDataStructure(node, node.name)))
        is TraitFunctionCallNode -> {
            val symbol = if (!(node.trait is VariableReferenceNode && node.trait.name== "self"))
                getExpressionType(block, node.trait, signatures).getRightOrNull()
                    ?: return Either.Left(AnalysisError.UndefinedVariable(node, node.trait.mark.value, block))
                else block.self
            if (symbol is Type.Trait) {
                val function = symbol.functions.firstOrNull { it.name == node.function }
                if (function != null) {
                    return Either.Right(function.returnType)
                }
                return Either.Left(AnalysisError.TraitForFunctionNotFound(node, node.trait.mark.value, node.function))
            }

            val (_, _, function) = figureOutTraitForVariable(
                block = block,
                variable = node.trait,
                signatures = signatures,
                call = node.function
            ) ?: return Either.Left(AnalysisError.TraitForFunctionNotFound(node, node.trait.mark.value, node.function))
            Either.Right(function.returnType)
        }
        is StructAccessNode -> {
            val struct = getExpressionType(
                block = block,
                node = node.struct,
                signatures = signatures
            ).getRightOrNull() ?: return Either.Left(AnalysisError.UndefinedDataStructure(node, node.struct.mark.value))

            when (struct) {
                is Type.Struct -> {
                    val field = struct.fields[node.field] ?: return Either.Left(AnalysisError.UndefinedField(node, node.field))
                    Either.Right(field)
                }
                else -> Either.Left(AnalysisError.NotADataStructure(node, struct))
            }
        }
        else -> error("Unknown node type $node")
    }
}

fun inferCallType(
    block: MemoryBlock,
    signatures: Signatures,
    node: CallNode
): Type {
    val function = signatures.functions.firstOrNull { it.name == node.name }
    if (function != null) {
        return function.returnType
    }
    return block.figureOutSymbol(node.name) ?: Type.Unknown
}

data class TraitFunctionMetadata(
    val trait: SignatureTrait,
    val impl: SignatureTraitImpl?,
    val function: SignatureFunction,
    val returnType: Type,
    val variableType: Type
)

fun figureOutTraitForVariable(
    block: MemoryBlock,
    variable: SyntaxTreeNode,
    signatures: Signatures,
    call: String
): TraitFunctionMetadata? {
    val resolvedVariable = (if (!isNodeSelf(variable)) getExpressionType(block ,variable, signatures).getRightOrNull() else block.self) ?: return null

    return signatures.traits.firstNotNullOfOrNull { trait ->
        val impl = if (resolvedVariable !is Type.Trait) trait.impls.firstOrNull {
            it.struct == resolvedVariable.signature
        } else null

        if (impl != null || resolvedVariable is Type.Trait) {
            val function = trait.functions.firstOrNull { it.name == call }
            if (function != null) {
                return TraitFunctionMetadata(trait, impl, function, function.returnType, resolvedVariable)
            }
        }
        null
    }
}

fun isNodeSelf(node: SyntaxTreeNode): Boolean {
    return node is VariableReferenceNode && node.name == "self"
}