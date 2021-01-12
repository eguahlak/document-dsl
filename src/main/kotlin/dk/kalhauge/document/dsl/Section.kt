package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext
import dk.kalhauge.document.dsl.structure.Prefixed
import dk.kalhauge.util.anchorize
import dk.kalhauge.util.normalize

class Section(
    context: Context?,
    title: String,
    label: String? = null,
    level: Int? = null,
    val customNumber: Int? = null
    ) : Block.BaseParent(), Block.Child, Target, Prefixed {
  override var context = context ?: FreeContext
  override var title = text(title)
  private val label = label ?: "sec=${title.anchorize()}"
  override val key get() = normalize(context.keyPath, label)
  override val filePath get() = context.filePath
  override val keyPath get() = context.keyPath
  override fun register(target: Target) = context.register(target)
  override fun find(key: String) = context.find(key)

  val number get() = customNumber ?:
      context.let { if (it is Block.Parent) it.numberOf(this) + 1 else 1 }
  override val prefix: String get() =
      context.let { if (it is Section) "${it.prefix}.$number " else "$number " }
  private val forcedLevel = level
  val level: Int get() = forcedLevel ?: context.let { if (it is Section) it.level + 1 else 1}

  init {
    register()
    }

  override fun register() {
    this.context.register(this)
    }
  override fun toString() = "{/Section $title/:$label}"

  override fun isEmpty() = children.isEmpty() && title.isEmpty()

  operator fun invoke(build: Section.() -> Unit) { build() }
  operator fun plusAssign(text: Text) { paragraph { add(text) }}
  operator fun plusAssign(content: String) { plusAssign(text(content)) }

  }

fun Block.BaseParent.section(
    title: String,
    label: String? = null,
    level: Int? = null,
    number: Int? = null,
    build: Section.() -> Unit = {}) =
  Section(this, title, label, level, number).also {
    it.build()
    add(it)
    }

fun section(
    title: String,
    label: String? = null,
    level: Int? = null,
    number: Int? = null,
    build: Section.() -> Unit = {}) =
  Section(null, title, label, level, number).also {
    it.build()
    }

fun Block.Parent.numberOf(section: Section) =
    children.filterIsInstance<Section>().indexOf(section)

class AnonymousSection(context: Context?) : Block.BaseParent() {
  val context = context ?: FreeContext

  override val children = mutableListOf<Block.Child>()

  override val filePath get() = context.filePath
  override val keyPath get() = context.keyPath
  override fun register(target: Target) = context.register(target)
  override fun find(key: String) = context.find(key)

  operator fun invoke(build: AnonymousSection.() -> Unit) { build() }
  // TODO align with Paragraph.plusAssign
  operator fun plusAssign(text: Text) { paragraph { add(text) } }
  operator fun plusAssign(content: String) { plusAssign(text(content)) }
  }

fun Block.BaseParent.anonymousSection() = AnonymousSection(this)

fun anonymousSection() = AnonymousSection(null)

fun Block.Parent.add(anonymousSection: AnonymousSection, fallback: String? = null) {
  if (anonymousSection.children.isEmpty() && fallback != null) add(paragraph(fallback))
  else anonymousSection.children.forEach { add(it) }
  }

