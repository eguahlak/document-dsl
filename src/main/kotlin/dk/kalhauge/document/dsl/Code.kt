package dk.kalhauge.document.dsl

class Code(val text: String, val language: String = "") : Block.Child {
  override fun isEmpty() = false
  }

fun Block.Parent.code(text: String, language: String = "") =
    Code(text.trimIndent(), language).also { add(it) }

fun Block.Parent.kotlin(text: String) =
    code(text, "kotlin")

fun Block.Parent.java(text: String) =
    code(text, "java")

fun Block.Parent.javascript(text: String) =
    code(text, "javascript")

fun Block.Parent.elm(text: String) =
    code(text, "elm")

fun Block.Parent.bash(text: String) =
    code(text, "bash")
