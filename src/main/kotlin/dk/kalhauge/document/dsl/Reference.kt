package dk.kalhauge.document.dsl

import dk.kalhauge.util.normalizePath

interface Target {
  val label: String
  fun fixFrom(document: Document) = this
  }

class TargetProxy(override val label: String) : Target {
  override fun fixFrom(document: Document): Target {
    val fullLabel = normalizePath("${document.path}/$label")
    return Context[fullLabel]
    }
  }

class Reference(
    var target: Target,
    val title: Text?
    ) : Inline {

  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title]($target.label)")
    }

  override fun isEmpty() = false

  override fun toString() = nativeString()

  data class Relation(val document: Document)

  }

fun Inline.Parent.reference(target: Target, title: Text?) =
    Reference(target, title).also { add(it) }

fun Inline.Parent.reference(label: String, title: Text?) =
    reference(TargetProxy(label), title)

fun Inline.Parent.reference(target: Target, title: String? = null) =
    Reference(target, title?.toText()).also { add(it) }

fun Inline.Parent.reference(label: String, title: String? = null) =
    reference(TargetProxy(label), title?.toText())


