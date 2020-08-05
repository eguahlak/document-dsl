package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext
import dk.kalhauge.document.dsl.structure.Inline
import dk.kalhauge.util.labelize
import dk.kalhauge.util.normalize
import dk.kalhauge.util.toMD5

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
  abstract fun toMD5(): String
  class Web(override val url: String) : Address() {
    override val label = "web=${after("//").labelize()}"
    override val title = after("//")
    override fun toMD5() = url.toMD5()
    }
  class Disk(val path: String) : Address(){
    override val url = "file://$path"
    override val title = after("/")
    override val label = "file=${title.labelize()}"
    override fun toMD5(): String {
      //println("Taking MD5 of ${path.substringAfter(CourseContext.root)}")
      return path.substringAfter(CourseContext.root).toMD5()
      }

    }
  /*
  object System : Address() {
    override val label = "system"
    override val url = "system://$label"
    override val title = "System resource"
    }
  */
  override fun toString() = """"$url""""
  }

open class Resource(
    context: Context?,
    val source: Address,
    title: String?,
    label: String?
    ) : Inline, Target {
  override var context = context ?: FreeContext
  // This cannot be invoked before document is assembled
  // override val key : String by lazy { normalize(document.key, label) }
  override val key get() = normalize(context.keyPath, label)

  override val title: Text = if (title == null) code(source.title) else text(title)

  val label = label ?: source.label

  init {
    register()
    }

  override fun register() {
    context.register(this)
    }

  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title](${source.url})")
    }
  override fun isEmpty() = false

  override fun toString() = "{$title:$label}"
  }


class CachedResource(
    context: Context?,
    source: Address,
    title: String?,
    label: String?,
    name: String?,
    render: Boolean?
    ) : Resource(context, source, title, label) {
  // val name: String = "${source.url.toMD5()}-${name ?: source.url.substringAfterLast("/")}"
  val name: String = "${source.toMD5()}-${name ?: source.url.substringAfterLast("/")}"
  val render: Boolean = render ?: when (source.after(".")) {
    "png", "img", "jpg", "jpeg" -> true
    else -> false
    }

  override fun nativeString(builder: StringBuilder) {
    builder.append("[$title](${source.url})")
    }

  }

fun Inline.BaseContainer.website(url: Address.Web, title: String? = null, label: String? = null) =
    Resource(this, url, title, label).also {
      reference(it)
      }

fun website(url: Address.Web, title: String? = null, label: String? = null) =
    Resource(null, url, title, label)

fun cached(
    source: Address,
    title: String? = null,
    label: String? = null,
    name: String? = null,
    render: Boolean? = null
    ) =
    CachedResource(null, source, title, label, name, render)


fun Inline.BaseContainer.cached(
    source: Address,
    title: String? = null,
    label: String? = null,
    name: String? = null,
    render: Boolean? = null
    ) =
    CachedResource(this, source, title, label, name, render).also {
      reference(it)
      }

fun Inline.BaseContainer.link(url: Address.Web, title: String? = null, label: String? = null, name: String? = null) =
  if (name == null) website(url, title, label)
  else cached(url, title, label, name, false)

fun link(url: Address.Web, title: String? = null, label: String? = null, name: String? = null) =
  if (name == null) website(url, title, label)
  else cached(url, title, label, name, false)

fun Inline.BaseContainer.image(url: Address, title: String? = null, label: String? = null, name: String? = null) =
  cached(url, title, label, name, true)

fun image(url: Address, title: String? = null, label: String? = null, name: String? = null) =
  cached(url, title, label, name, true)
