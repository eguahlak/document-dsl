package dk.kalhauge.document.dsl

class Paragraph: Block.Child, Inline.Parent {
  override val parts = mutableListOf<Inline>()
  override fun add(part: Inline) {
    parts += part
    }
  operator fun invoke(build: Paragraph.() -> Unit) { build() }
  operator fun plusAssign(text: Text) { add(text) }
  operator fun plusAssign(content: String) { text(content) }

  override fun isEmpty() = parts.isEmpty()
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

