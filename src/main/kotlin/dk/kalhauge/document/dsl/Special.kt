package dk.kalhauge.document.dsl

interface Special {
  fun process()
  }

class TableOfContent(
    val parent: Document,
    val depth: Int,
    title: String,
    val level: Int
    ): Section(title, "toc"), Special {

  init {
    parent.add(this)
    }

  private fun Listing.create(block: Block.Parent, depth: Int) {
    block.children.filterIsInstance<Section>().filter{ it !is Special }.forEach { section ->
      paragraph { reference(section) }
      if (depth > 0) list {
        create(section, depth - 1)
        }
      }
    }

  override fun process() {
    if (depth > 0) list {
      create(parent, depth - 1)
      }
    }

  }

fun Document.toc(depth: Int = 1, title: String = "Contents", level: Int = 3) =
  TableOfContent(this, depth, title, level)