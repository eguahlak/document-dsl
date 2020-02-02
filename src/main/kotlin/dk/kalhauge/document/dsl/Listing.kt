package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.Listing.Type
import dk.kalhauge.document.dsl.Listing.Type.*
import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext

// depth
class Listing(context: Context?, val type: Type) : Block.BaseParent(), Block.Child {
  override var context = context ?: FreeContext
  override val filePath get() = context.filePath
  override val keyPath get() = context.keyPath
  override fun register(target: Target) = context.register(target)
  override fun find(key: String) = context.find(key)

  enum class Type { BULLETED, ARABIC, ROMAN, ALPHABETIC }

  override fun isEmpty() = children.isEmpty()

  }

fun Block.BaseParent.list(type: Type = BULLETED, build: Listing.() -> Unit = {}) =
    Listing(this, type).also {
      it.build()
      add(it)
      }

fun Block.BaseParent.enumeration(type: Type = ARABIC, build: Listing.() -> Unit = {}) =
    Listing(this, type).also {
      it.build()
      add(it)
      }

fun list(type: Type = BULLETED, build: Listing.() -> Unit = {}) =
    Listing(null, type).also { it.build() }

fun enumeration(type: Type = ARABIC, build: Listing.() -> Unit = {}) =
    Listing(null, type).also { it.build() }



