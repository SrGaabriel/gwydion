package me.gabriel.gwydion.lexing

import me.gabriel.gwydion.lexing.lexers.StringLexer

enum class TokenKind {
    NUMBER,
    PLUS,
    MINUS,
    TIMES,
    DIVIDE,
    PLUS_ASSIGN,
    MINUS_ASSIGN,
    TIMES_ASSIGN,
    DIVIDE_ASSIGN,
    RETURN,
    SEMICOLON,
    ASSIGN,
    COMMA,
    OPENING_PARENTHESES,
    CLOSING_PARENTHESES,
    FUNCTION,
    IDENTIFIER,
    OPENING_BRACES,
    CLOSING_BRACES,
    INTRINSIC,
    STRING,
    INT8_TYPE,
    INT16_TYPE,
    INT32_TYPE,
    INT64_TYPE,
    UINT8_TYPE,
    UINT16_TYPE,
    UINT32_TYPE,
    UINT64_TYPE,
    FLOAT32_TYPE,
    FLOAT64_TYPE
}

val TYPE_TOKENS = listOf(
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
    TokenKind.IDENTIFIER
)

data class Token(
    val kind: TokenKind,
    val value: String,
    val position: Int
)