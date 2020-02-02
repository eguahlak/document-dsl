package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext
import dk.kalhauge.document.dsl.structure.Inline

class Paragraph(context: Context?): Inline.BaseContainer(), Block.Child {
  override var context = context ?: FreeContext
  override val filePath get() = context.filePath
  override val keyPath get() = context.keyPath
  override fun register(target: Target) = context.register(target)
  override fun find(key: String) = context.find(key)

  operator fun invoke(build: Paragraph.() -> Unit) { build() }
  operator fun plusAssign(text: Text) { add(text) }
  operator fun plusAssign(content: String) { text(content) }

  override fun isEmpty() = parts.isEmpty()
  }

fun Block.BaseParent.paragraph(content: String? = null, build: Paragraph.() -> Unit = {}) =
    Paragraph(this).also { paragraph ->
      content?.let { content -> paragraph.add(text(content)) }
      paragraph.build()
      add(paragraph)
      }

fun Block.BaseParent.paragraph(text: Text, build: Paragraph.() -> Unit = {}) =
    Paragraph(this).also {
      it.add(text)
      it.build()
      add(it)
      }

fun paragraph(content: String? = null, build: Paragraph.() -> Unit = {}) =
    Paragraph(null).also { paragraph ->
      content?.let { content -> paragraph.add(text(content)) }
      paragraph.build()
      }

fun paragraph(text: Text, build: Paragraph.() -> Unit = {}) =
    Paragraph(null).also {
      it.add(text)
      it.build()
      }

