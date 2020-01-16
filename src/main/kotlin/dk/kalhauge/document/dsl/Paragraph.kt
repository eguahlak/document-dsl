package dk.kalhauge.document.dsl

class Paragraph(parent: Block.Parent, val space: String = " ") : Block.Child(parent), Inline.Parent {
  override val parts = mutableListOf<Inline>()

  init {
    parent.add(this)
    }

  override fun add(part: Inline) { parts += part }

  override fun print(indent: String) {
    println("${indent}Paragraph")
    parts.forEach { println("$indent  ${it.nativeString()}") }
    }

  }

fun Block.Parent.paragraph(content: String? = null, build: Paragraph.() -> Unit = {}) =
  Paragraph(this).also { p ->
    content?.let { c -> p.add(text(c)) }
    p.build()
    }

