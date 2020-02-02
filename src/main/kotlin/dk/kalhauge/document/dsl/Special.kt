package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext


interface Special

class TableOfContent(
    context: Context?,
    val document: Document,
    val depth: Int
    ): Listing(context, Listing.Type.BULLETED), Special {
  override var context: Context = FreeContext
  override fun isEmpty() = false

  private fun Listing.create(block: Block.Parent?, depth: Int) {
    if (block == null) return
    block.children.filterIsInstance<Section>().forEach { section ->
      paragraph { reference(section) }
      if (depth > 0) list {
        create(section, depth - 1)
        }
      }
    }

  fun generate(): Listing {
    if (depth > 0) list {
      create(document, depth - 1)
      }
    return this
    }

  }

fun Document.toc(depth: Int = 2) =
  TableOfContent(this, this, depth).also {
    add(it)
    }
