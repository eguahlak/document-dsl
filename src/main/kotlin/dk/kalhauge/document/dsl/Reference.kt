package dk.kalhauge.document.dsl

class Reference(
    override val document: Document?,
    target: Target?,
    val label: String,
    val title: String?
) : Inline {
  var target: Target? = target
    get(): Target? {
      if (field == null) field = document?.findTarget(label)
      return field
    }
    set(value) { field = value }
  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title]($label)")
  }
  override fun nakedString(builder: StringBuilder) {
    builder.append("[$title]($label)")
  }
}

fun Inline.Parent.reference(label: String, title: String? = null) =
    Reference(document, null, label, title).also { this.add(it) }

fun Inline.Parent.reference(target: Target, title: String? = null) =
    Reference(document, target, target.label, title).also { this.add(it) }

interface Target {
  val label: String
  }
