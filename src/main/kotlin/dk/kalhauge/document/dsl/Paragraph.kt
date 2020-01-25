package dk.kalhauge.document.dsl

class Paragraph: Block.Child, Inline.Parent {
  override val parts = mutableListOf<Inline>()
  override fun add(part: Inline) {
    parts += part
    }
  }

fun Block.Parent.paragraph(content: String? = null, build: Paragraph.() -> Unit = {}) =
    Paragraph().also { paragraph ->
      content?.let { content -> paragraph.add(text(content)) }
      paragraph.build()
      add(paragraph)
      }

fun Block.Parent.paragraph(text: Text, build: Paragraph.() -> Unit = {}) =
    Paragraph().also {
      it.add(text)
      it.build()
      add(it)
      }

