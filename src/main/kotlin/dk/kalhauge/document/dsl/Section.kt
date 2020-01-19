package dk.kalhauge.document.dsl

import dk.kalhauge.util.anchorize

class Section(title: String, label: String): Block.Child, Block.Parent, Target {
  var title = text(title)
  override val label = "sec:$label"
  override val children = mutableListOf<Block.Child>()
  override fun add(child: Block.Child? ) { if (child != null) children += child }

  data class Relation(val document: Document, val level: Int, val number: Int, val prefix: String)
  }

fun Block.Parent.section(title: String, label: String? = null, build: Section.() -> Unit = {}) =
    Section(title, label ?: title.anchorize()).also {
      it.build()
      add(it)
      }

