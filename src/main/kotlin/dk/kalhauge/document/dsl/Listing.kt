package dk.kalhauge.document.dsl


class Listing(parent: Block.Parent, val type: Type) : Block.Child(parent), Block.Parent {
  enum class Type { BULLETED, ARABIC, ROMAN, ALPHABETIC }

  override val children = mutableListOf<Block.Child>()
  val depth: Int
    get() = if (parent is Listing) parent.depth + 1 else 0

  init {
    parent.add(this)
    }

  override fun add(child: Block.Child) { children += child }

  override fun print(indent: String) {
    println("${indent}Listing")
    super.print(indent)
    }

  }

fun Block.Parent.list(
    type: Listing.Type = Listing.Type.BULLETED,
    build: Listing.() -> Unit = {}
    ) = Listing(this, type).also(build)

