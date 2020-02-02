package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext
import dk.kalhauge.document.dsl.structure.Inline
import dk.kalhauge.util.from
import dk.kalhauge.util.normalize

interface Target {
  val key: String
  val title: Text
  fun register()
  fun fixate(): Target = this
  }

class TargetProxy(context: Context?, val label: String) : Target {
  var context = context ?: FreeContext
  override val key get() = normalize(context.keyPath, label)
  override val title = text("<Proxy target for $label>")
  override fun fixate() = context.find(key)
  override fun register() {
    context.register(this)
    }
  }

class UnknownTarget(val reason: String, override val key: String) : Target {
  override fun fixate() = this
  override val title = text("<Unknown target: $key, $reason>")
  override fun toString() = "<Unknown target: $key, $reason>"
  override fun register() { }
  }

class Reference(context: Context?,
    var target: Target,
    val title: Text?
    ) : Inline {
  override var context = context ?: FreeContext

  override fun nativeString(builder: StringBuilder) {
    builder.append("{$title:${target.key}}")
    }

  override fun isEmpty() = false
  }

fun Inline.BaseContainer.reference(target: Target, title: Text?) =
    Reference(this, target, title).also { add(it) }

fun Inline.BaseContainer.reference(label: String, title: Text?) =
    reference(TargetProxy(this, label), title)

fun Inline.BaseContainer.reference(target: Target, title: String? = null) =
    Reference(this, target, title?.toText()).also { add(it) }

fun Inline.BaseContainer.reference(label: String, title: String? = null) =
    reference(TargetProxy(this, label), title?.toText())


