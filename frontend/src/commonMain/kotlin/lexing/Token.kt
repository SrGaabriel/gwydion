package me.gabriel.gwydion.frontend.lexing

import kotlinx.serialization.Serializable
import me.gabriel.gwydion.frontend.parsing.Modifiers

@Serializable
enum class TokenKind {
    NUMBER,
    PLUS,
    MINUS,
    TIMES,
    DIVIDE,
    EQUALS,
    PLUS_ASSIGN,
    MINUS_ASSIGN,
    TIMES_ASSIGN,
    DIVIDE_ASSIGN,
    RETURN,
    SEMICOLON,
    DECLARATION,
    COMMA,
    DOT,
    AT,
    RANGE,
    OPENING_BRACKETS,
    CLOSING_BRACKETS,
    OPENING_PARENTHESES,
    CLOSING_PARENTHESES,
    FUNCTION,
    IDENTIFIER,
    OPENING_BRACES,
    CLOSING_BRACES,
    INTRINSIC,
    STRING_START,
    STRING_TEXT,
    STRING_EXPRESSION_REFERENCE,
    STRING_END,
    IF,
    FOR,
    IN,
    ELSE,
    INTERNAL,
    TYPE_DECLARATION,
    RETURN_TYPE_DECLARATION,
    VOID,
    ANY_TYPE,
    INT8_TYPE,
    INT16_TYPE,
    INT32_TYPE,
    INT64_TYPE,
    UINT8_TYPE,
    UINT16_TYPE,
    UINT32_TYPE,
    UINT64_TYPE,
    FLOAT32_TYPE,
    FLOAT64_TYPE,
    STRING_TYPE,
    BOOL_TYPE,
    DATA,
    TRAIT,
    MAKE,
    INTO,
    SELF,
    MUT,
    MUTATION,
    EOF,
    BOF
}

val TYPE_TOKENS = listOf(
    TokenKind.ANY_TYPE,
    TokenKind.VOID,
    TokenKind.INT8_TYPE,
    TokenKind.INT16_TYPE,
    TokenKind.INT32_TYPE,
    TokenKind.INT64_TYPE,
    TokenKind.UINT8_TYPE,
    TokenKind.UINT16_TYPE,
    TokenKind.UINT32_TYPE,
    TokenKind.UINT64_TYPE,
    TokenKind.FLOAT32_TYPE,
    TokenKind.FLOAT64_TYPE,
    TokenKind.STRING_TYPE,
    TokenKind.BOOL_TYPE,
    TokenKind.IDENTIFIER
)

val MODIFIER_TOKENS = mapOf(
    TokenKind.INTRINSIC to Modifiers.INTRINSIC,
    TokenKind.INTERNAL to Modifiers.INTERNAL
)

@Serializable
data class Token(
    val kind: TokenKind,
    val value: String,
    val position: Int
)