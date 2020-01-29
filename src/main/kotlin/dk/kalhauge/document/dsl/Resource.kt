package dk.kalhauge.document.dsl

import dk.kalhauge.util.toMD5
import dk.kalhauge.document.dsl.Target

sealed class Address {
  abstract val label: String
  abstract val title: String
  abstract val url: String
  fun after(delimiter: String) = url.substringAfterLast(delimiter)
  companion object {
    operator fun invoke(source: String) =
      if (
        source.startsWith("http://", ignoreCase = true) ||
        source.startsWith("https://", ignoreCase = true)
        )  Web(source)
      else Disk(source)
    }
  class Web(override val url: String) : Address() {
    override val label = "web-${after("//")}"
    override val title = after("//")
    }
  class Disk(val path: String) : Address(){
    override val url = "file://$path"
    override val label = "disk-$path"
    override val title = after("/")
    }
  object System : Address() {
    override val label = "system"
    override val url = "system://$label"
    override val title = "System resource"
    }
  override fun toString() = """"$url""""
  }

open class Resource(
    val source: Address,
    title: String?,
    label: String?
    ) : Inline, Target {
  val title: Text
  final override val label: String

  init {
    if (title == null) this.title = code(source.title)
    else this.title = text(title)
    this.label = label ?: "res:${source.label}"
    Context.targets[this.label] = this
    }

  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title](${source.url})")
    }
  override fun isEmpty() = false

  override fun toString() = """Resource(source=$source, title="$title", label="$label")"""
  }

class CachedResource(
    source: Address,
    title: String?,
    label: String?,
    name: String?,
    render: Boolean?
    ) : Resource(source, title, label) {
  val name: String = "${source.url.toMD5()}-${name ?: source.url.substringAfterLast("/")}"
  val render: Boolean = render ?: when (source.after(".")) {
    "png", "img", "jpg", "jpeg" -> true
    else -> false
    }
  val follow get() = !render

  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title](${source.url})")
    }

  }

fun website(url: String, title: String? = null, label: String? = null) =
    Resource(Address.Web(url), title, label)

fun Inline.Parent.website(url: String, title: String? = null, label: String? = null) =
    Resource(Address.Web(url), title, label).also { reference(it) }


fun cached(
    source: String,
    title: String? = null,
    label: String? = null,
    name: String? = null,
    render: Boolean? = null
    ) =
    CachedResource(Address(source), title, label, name, render)

fun Inline.Parent.cached(
    source: String,
    title: String? = null,
    label: String? = null,
    name: String? = null,
    render: Boolean? = null
    ) =
    CachedResource(Address(source), title, label, name, render).also {
      reference(it)
      }

fun Inline.Parent.link(url: String, title: String? = null, label: String? = null, name: String? = null) =
  if (name == null) website(url, title, label)
  else cached(url, title, label, name, false)

fun Inline.Parent.image(url: String, title: String? = null, label: String? = null, name: String? = null) =
  cached(url, title, label, name, true)

fun link(url: String, title: String? = null, label: String? = null, name: String? = null) =
  if (name == null) website(url, title, label)
  else cached(url, title, label, name, false)

fun image(url: String, title: String? = null, label: String? = null, name: String? = null) =
  cached(url, title, label, name, true)
