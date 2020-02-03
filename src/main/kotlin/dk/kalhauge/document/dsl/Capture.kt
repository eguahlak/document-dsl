package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext
import dk.kalhauge.document.dsl.structure.Inline
import dk.kalhauge.util.anchorize
import dk.kalhauge.util.normalize

class Capture(
    context: Context?,
    title: String,
    label: String? = null
    ) : Inline.BaseContainer(), Block.Child, Target {
  override var context = context ?: FreeContext
  override var title = freeText(title)
  private val label = label ?: "cap=${title.anchorize()}"
  override val key get() = normalize(context.keyPath, label)
  override val filePath get() = context.filePath
  override val keyPath get() = context.keyPath
  override fun register(target: Target) = context.register(target)
  override fun find(key: String) = context.find(key)
  init {
    register()
    }

  override fun register() {
    this.context.register(this)
    }
  override fun toString() = "{$title/:$label}"
  override fun isEmpty() = parts.isEmpty() && title.isEmpty()
  }

fun Block.BaseParent.capture(title: String, label: String? = null, build: Capture.() -> Unit = {}) =
    Capture(this, title, label).also {
      it.build()
      add(it)
      }

fun capture(title: String, label: String? = null, build: Capture.() -> Unit = {}) =
    Capture(null, title, label).also {
      it.build()
      }

