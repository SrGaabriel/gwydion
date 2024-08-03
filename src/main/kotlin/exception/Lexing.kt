package me.gabriel.gwydion.exception

// I won't use exceptions because they generate too much overhead by default, and I'm not going to use them for control flow.
sealed class LexingError(val message: String, val position: Int) {
    class UnknownToken(token: String, position: Int) : LexingError("unknown token `$token`", position)
}