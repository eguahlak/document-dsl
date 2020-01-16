package dk.kalhauge.document.dsl

import dk.kalhauge.util.toMD5

sealed class Address(val path: String) {
  companion object {
    operator fun invoke(path: String): Address {
      if (
          path.startsWith("http://", ignoreCase = true) ||
          path.startsWith("https://", ignoreCase = true)
          ) return Web(path)
      else return Disk(path)
      }
    }
  class Web(path: String) : Address(path)
  class Disk(path: String) : Address(path)
  }

class Resource(
    override val document: Document?,
    val source: Address,
    label: String?,
    title: String?
    ): Inline, Target {
  enum class Type { LINK, DOCUMENT, IMAGE }
  var type = Type.LINK
  var title: Text
  var link: String? = null
  override val label =
    if (label != null) "res:$label"
    else "res:${Folder.targets.filter { (label, target) -> target is Resource }.size + 1}"


  init {
    Folder.targets[this.label] = this
    if (title != null) this.title = text(title)
    else this.title = code(source.path)
    }

  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title](${source.path})")
    }

  override fun nakedString(builder: StringBuilder) {
    builder.append("[$title](${source.path})")
    }

  override fun toString() = "Resource: $title -> ${source.path}"

  }

fun Inline.Parent.resource(
    source: Address,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) =
  Resource(document, source, title, label).also {
    it.build()
    reference(it.label)
    }

fun Document.resource(
    source: Address,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) = Resource(this, source, title, label).also(build)

fun Inline.Parent.link(
    url: String,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) =
  Resource(document, Address.Web(url), title, label).also {
    it.type = Resource.Type.LINK
    it.build()
    reference(it)
    }

fun Document.link(
    url: String,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) =
  Resource(this, Address.Web(url), title, label).apply {
    type = Resource.Type.LINK
    build()
    }

fun Inline.Parent.document(
    url: String,
    link: String,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) =
  Resource(document, Address(url), title, label).also {
    it.type = Resource.Type.DOCUMENT
    it.link = "${url.toMD5()}-${link}"
    it.build()
    reference(it)
    }

fun Document.document(
    url: String,
    link: String,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) =
  Resource(document, Address(url), title, label).apply {
    type = Resource.Type.DOCUMENT
    this.link = "${url.toMD5()}-${link}"
    build()
    }

fun Inline.Parent.image(
    url: String,
    link: String,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) =
  Resource(document, Address(url), title, label).also {
    it.type = Resource.Type.IMAGE
    it.link = "${url.toMD5()}-${link}"
    it.build()
    reference(it)
    }

fun Document.image(
    url: String,
    link: String,
    label: String? = null,
    title: String? = null,
    build: Resource.() -> Unit = {}
    ) =
  Resource(document, Address(url), title, label).apply {
    type = Resource.Type.IMAGE
    this.link = "${url.toMD5()}-${link}"
    build()
    }