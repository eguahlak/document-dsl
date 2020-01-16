package dk.kalhauge.document.dsl

class Code(parent: Block.Parent, val text: String, val language: String = "") : Block.Child(parent) {

  init {
    parent.add(this)
    }

  override fun print(indent: String) {
    println("${indent}Code:")
    println(text.prependIndent("$indent  |"))
    }

  }

fun Block.Parent.code(text: String, language: String = "") =
  Code(this, text.trimIndent(), language)

fun Block.Parent.kotlin(text: String) =
  code(text, "kotlin")

fun Block.Parent.java(text: String) =
  code(text, "java")

fun Block.Parent.javascript(text: String) =
  code(text, "javascript")

fun Block.Parent.elm(text: String) =
  code(text, "elm")

