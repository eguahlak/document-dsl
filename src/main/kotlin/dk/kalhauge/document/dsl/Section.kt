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
    level: Int? = null
    ) : Block.BaseParent(), Block.Child, Target, Prefixed {
  override var context = context ?: FreeContext
  override var title = text(title)
  private val label = label ?: "sec=${title.anchorize()}"
  override val key get() = normalize(context.keyPath, label)
  override val filePath get() = context.filePath
  override val keyPath get() = context.keyPath
  override fun register(target: Target) = context.register(target)
  override fun find(key: String) = context.find(key)

  val number get() = context.let { if (it is Block.Parent) it.numberOf(this) + 1 else 1 }
  override val prefix get() = context.let { if (it is Section) "${it.number}.$number " else "$number " }
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

fun Block.BaseParent.section(title: String, label: String? = null, hashes: Int? = null, build: Section.() -> Unit = {}) =
    Section(this, title, label, hashes).also {
      it.build()
      add(it)
      }

fun section(title: String, label: String? = null, hashes: Int? = null, build: Section.() -> Unit = {}) =
    Section(null, title, label, hashes).also {
      it.build()
      }

fun Block.Parent.numberOf(section: Section) =
    children.filterIsInstance<Section>().indexOf(section)

