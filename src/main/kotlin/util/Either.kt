package me.gabriel.gwydion.util

/**
 * Represents a value of one of two possible types (a disjoint union).
 */
sealed class Either<Left, Right> private constructor() {
    /**
     * Represents the left side of [Either] class which by convention is a "Left" value.
     * Usually the less desirable outcome.
     *
     * @param value The value of the right side.
     * @constructor Creates a right side of [Either] class.
     */
    class Left<Left, Right>(val value: Left) : Either<Left, Right>() {
        override fun <T> fold(ifLeft: (Left) -> T, ifRight: (Right) -> T): T {
            return ifLeft(value)
        }
    }

    /**
     * Represents the right side of [Either] class which by convention is a "Right" value.
     * Usually the desirable outcome.
     *
     * @param value The value of the right side.
     * @constructor Creates a right side of [Either] class.
     */
    class Right<Left, Right>(val value: Right) : Either<Left, Right>() {
        override fun <T> fold(ifLeft: (Left) -> T, ifRight: (Right) -> T): T {
            return ifRight(value)
        }
    }

    /**
     * Applies the given function [ifLeft] if this is a [Left] or the given function [ifRight] if this is a [Right].
     *
     * @param ifLeft the function to apply if this is a [Left]
     * @param ifRight the function to apply if this is a [Right]
     * @return the results of applying the function
     */
    abstract fun <T> fold(ifLeft: (Left) -> T, ifRight: (Right) -> T): T

    /**
     * Returns `true` if this is a [Left], `false` otherwise.
     */
    fun isLeft(): Boolean = fold({ true }, { false })

    /**
     * Returns `true` if this is a [Right], `false` otherwise.
     */
    fun isRight(): Boolean = fold({ false }, { true })

    /**
     * Returns the left value if it is present, otherwise `null`.
     */
    fun getLeftOrNull(): Left? = fold({ it }, { null })

    /**
     * Returns the right value if it is present, otherwise `null`.
     */
    fun getRightOrNull(): Right? = fold({ null }, { it })

    /**
     * Returns the left value if it is present, otherwise throws [NoSuchElementException].
     */
    fun getLeft(): Left = fold({ it }, { throw NoSuchElementException("Either.getLeft() on Right") })

    /**
     * Returns the right value if it is present, otherwise throws [NoSuchElementException].
     */
    fun getRight(): Right = fold({ throw NoSuchElementException("Either.getRight() on Left") }, { it })

    /**
     * Returns the left value if it is present, otherwise [default].
     */
    fun getLeftOrDefault(default: Left): Left = fold({ it }, { default })

    /**
     * Returns the right value if it is present, otherwise [default].
     */
    fun getRightOrDefault(default: Right): Right = fold({ default }, { it })

    /**
     * Returns the left value if it is present, otherwise [default].
     */
    fun getLeftOrElse(default: (Right) -> Left): Left = fold({ it }, default)

    /**
     * Returns the right value if it is present, otherwise [default].
     */
    fun getRightOrElse(default: (Left) -> Right): Right = fold(default, { it })

    /**
     * Maps the left value if it is present.
     *
     * @param transform the function to apply if this is a [Left]
     * @return a new [Either] instance with the transformed value
     */
    fun <NewLeft> mapLeft(transform: (Left) -> NewLeft): Either<NewLeft, Right> = fold({ Left(transform(it)) }, { Right(it) })

    /**
     * Maps the right value if it is present.
     *
     * @param transform the function to apply if this is a [Right]
     * @return a new [Either] instance with the transformed value
     */
    fun <NewRight> mapRight(transform: (Right) -> NewRight): Either<Left, NewRight> = fold({ Left(it) }, { Right(transform(it)) })

    /**
     * Returns the result of applying the given [transform] function to an element of this [Either].
     */
    fun <NewLeft, NewRight> map(transformLeft: (Left) -> NewLeft, transformRight: (Right) -> NewRight): Either<NewLeft, NewRight> = fold({ Left(transformLeft(it)) }, { Right(transformRight(it)) })

    /**
     * Swap the left and right types of this [Either].
     */
    fun swap(): Either<Right, Left> = fold({ Right(it) }, { Left(it) })

    /**
     * Returns the right value if it is present, otherwise throws an exception
     */
    fun unwrap(): Right = getRight()

    /**
     * Maps the left value if it is present.
     */
    inline fun ifLeft(block: (Left) -> Unit): Either<Left, Right> {
        if (isLeft()) block(getLeft())
        return this
    }

    /**
     * Folds the left value if it is present.
     */
    inline fun foldIfLeft(block: (Either<Left, Right>) -> Unit) {
        if (isLeft()) block(this)
    }

    companion object {
        fun <Left, Right> left(value: Left): Either<Left, Right> = Left(value)
        fun <Left, Right> right(value: Right): Either<Left, Right> = Right(value)
    }
}