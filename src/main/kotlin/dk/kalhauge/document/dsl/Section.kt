package dk.kalhauge.document.dsl

import dk.kalhauge.util.anchorize

open class Section(title: String, label: String): Block.Child, Block.Parent, Target {
  var title = text(title)
  override val label = "sec:$label"
  override val children = mutableListOf<Block.Child>()

  override fun add(child: Block.Child? ) { if (child != null && !child.isEmpty()) children += child }

  override fun toString() = """Section("$title"/$label)"""

  override fun isEmpty() = children.isEmpty() && title.isEmpty()

  operator fun invoke(build: Section.() -> Unit) { build() }
  operator fun plusAssign(text: Text) { paragraph { add(text) }}
  operator fun plusAssign(content: String) { plusAssign(text(content)) }

  data class Relation(val document: Document, val level: Int, val number: Int, val prefix: String)
  }

fun Block.Parent.section(title: String, label: String? = null, build: Section.() -> Unit = {}) =
    Section(title, label ?: title.anchorize()).also {
      it.build()
      add(it)
      }

