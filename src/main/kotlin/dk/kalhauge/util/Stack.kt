package dk.kalhauge.util

import java.util.*

fun <T> stackOf(vararg elements: T): Stack<T> {
  val stack = Stack<T>()
  elements.forEach { stack.push(it) }
  return stack
  }