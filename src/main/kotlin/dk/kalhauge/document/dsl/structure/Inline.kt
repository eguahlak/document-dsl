package dk.kalhauge.document.dsl.structure

import dk.kalhauge.document.dsl.text

interface Inline {
  var context: Context
  fun isEmpty(): Boolean
  fun nativeString(builder: StringBuilder)
  fun nativeString() = buildString { nativeString(this) }

  interface Container {
    val parts: List<Inline>
    fun add(part: Inline)
    }

  abstract class BaseContainer() : Container, Context {
    override val parts = mutableListOf<Inline>()
    override fun add(part: Inline) {
      parts += part
      part.context = this
      }
    operator fun String.unaryPlus() {
      text(this)
      }
    }

  }

