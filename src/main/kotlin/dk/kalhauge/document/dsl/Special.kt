package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext


interface Special

class TableOfContent(
    val document: Document,
    val depth: Int
    ): Block.Child, Special {
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

  fun Block.Parent.generate() =
    list {
      if (depth > 0) list {
        create(document, depth - 1)
        }
      }

  }

fun Document.toc(depth: Int = 2) =
  TableOfContent(this, depth).also {
    add(it)
    }
