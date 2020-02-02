package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext

class Code(context: Context?, val text: String, val language: String = "") : Block.Child {
  override var context = context ?: FreeContext
  override fun isEmpty() = false
  }

fun Block.BaseParent.code(text: String, language: String = "") =
    Code(this, text.trimIndent(), language).also { add(it) }

fun Block.BaseParent.kotlin(text: String) =
    code(text, "kotlin")

fun Block.BaseParent.java(text: String) =
    code(text, "java")

fun Block.BaseParent.javascript(text: String) =
    code(text, "javascript")

fun Block.BaseParent.elm(text: String) =
    code(text, "elm")

fun Block.BaseParent.bash(text: String) =
    code(text, "bash")
