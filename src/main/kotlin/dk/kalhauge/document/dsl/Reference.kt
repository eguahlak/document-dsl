package dk.kalhauge.document.dsl

class Reference(
    var target: Target?,
    val label: String,
    val title: String?
    ) : Inline {

  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title]($label)")
    }
  override fun isEmpty() = false
  override fun toString() = nativeString()

  data class Relation(val document: Document)

  }

fun Inline.Parent.reference(label: String, title: String? = null) =
    Reference(null, label, title).also { this.add(it) }

fun Inline.Parent.reference(target: Target, title: String? = null) =
    Reference(target, target.label, title).also { this.add(it) }

interface Target {
  val label: String
  }
