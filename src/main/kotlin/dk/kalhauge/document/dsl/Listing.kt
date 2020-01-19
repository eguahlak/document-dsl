package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.Listing.Type
import dk.kalhauge.document.dsl.Listing.Type.*

// depth
class Listing(val type: Type) : Block.Child, Block.Parent {
  enum class Type { BULLETED, ARABIC, ROMAN, ALPHABETIC }

  override val children = mutableListOf<Block.Child>()

  override fun add(child: Block.Child) { children += child }

  }

fun Block.Parent.list(type: Type = BULLETED, build: Listing.() -> Unit = {}) =
    Listing(type).also {
      it.build()
      add(it)
      }

fun Block.Parent.enumeration(type: Type = ARABIC, build: Listing.() -> Unit = {}) =
    Listing(type).also {
      it.build()
      add(it)
      }



