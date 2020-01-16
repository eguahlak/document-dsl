package dk.kalhauge.util

class Path<T>(val first: T, val rest: Path<T>?) : Iterable<T> {
  override fun iterator(): Iterator<T> = iterator {
    yield(first)
    if (rest != null) yieldAll(rest)
    }

  fun joinToString(char: Char): String =
      if (rest == null) "$first" else "$first$char${rest.joinToString(char)}"

  override fun toString() = "$first --> $rest"

  }

fun <T> pathOf(vararg steps: T): Path<T>? =
    steps.foldRight(null) { step: T, acc: Path<T>? -> Path<T>(step, acc) }

fun <T> pathOf(steps: List<T>): Path<T>? =
    steps.foldRight(null) { step: T, acc: Path<T>? -> Path<T>(step, acc) }
