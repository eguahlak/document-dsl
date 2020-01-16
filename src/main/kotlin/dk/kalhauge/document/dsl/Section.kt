package dk.kalhauge.document.dsl

class Section(parent: Block.Parent, title: String?, label: String?) : Block.Child(parent), Block.Parent, Target {
  var title: Text = text(title)
  override val children = mutableListOf<Block.Child>()
  private var position = parent.children.filter { it is Section }.size + 1
  var number: String = if (parent is Section) "${parent.number}.$position" else "$position"

  override val label = if (label == null) "sec:$number" else "sec:$label"

  init {
    parent.add(this)
    }

  override fun add(child: Block.Child) {
    children += child
    }

  override fun print(indent: String) {
    println("${indent}Section: ${title.nativeString()}")
    super.print(indent)
    }

  override fun toString(): String = "Section: $title"

  }

fun Block.Parent.section(title: String? = null, label: String? = null, build: Section.() -> Unit = {}) =
    Section(this, title, label).also(build)

