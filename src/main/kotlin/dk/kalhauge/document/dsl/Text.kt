package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.Text.Format.*
import dk.kalhauge.document.dsl.Text.Format
import dk.kalhauge.document.dsl.structure.*

const val EOT = '\u0000'
const val delimiters = "$EOT*/_~`{"

class Text(context: Context?, val format: Format = NORMAL) : Inline.BaseContainer(), Inline {
  override var context = context ?: FreeContext
  override val filePath get() = this.context.filePath
  override val keyPath get() = this.context.keyPath
  override fun register(target: Target) = this.context.register(target)
  override fun find(key: String): Target = this.context.find(key)

  enum class Format(val delimiter: Char) {
    NORMAL(EOT), BOLD('*'), ITALIC('/'), UNDERLINE('_'), STRIKE('~'), CODE('`')
    }

  override fun isEmpty() = parts.isEmpty() || (parts.size == 1 && parts[0].isEmpty())

  override fun nativeString(builder: StringBuilder) {
    if (format != NORMAL) builder.append(format.delimiter)
    parts.forEach { it.nativeString(builder) }
    if (format != NORMAL) builder.append(format.delimiter)
    }

  private fun consume(chars: CharIterator, builder: StringBuilder): Char {
    while(chars.hasNext()) {
      val next = chars.next()
      if (next in delimiters) return next
      builder.append(next)
      }
    return EOT
    }

  fun readContent(chars: CharIterator) {
    val target = this
    val builder = StringBuilder()
    val delimiter = consume(chars, builder)
    if (builder.isNotEmpty()) add(Content(this, builder.toString()))
    when (delimiter) {
      format.delimiter -> return
      EOT -> return
      '*' -> Text(this, BOLD).also { target.add(it) }.readContent(chars)
      '/' -> Text(this, ITALIC).also { target.add(it) }.readContent(chars)
      '_' -> Text(this, UNDERLINE).also { target.add(it) }.readContent(chars)
      '~' -> Text(this, STRIKE).also { target.add(it) }.readContent(chars)
      '`' -> Text(this, CODE).also { target.add(it) }.readContent(chars)
      '{' -> {
        readReference(chars)
        target.readContent(chars)
        }
      else -> IllegalStructure("Unknown delimiter $delimiter")
      }
    readContent(chars)
    }

  private fun readReference(chars: CharIterator): Reference {
    val builder = StringBuilder()
    while (chars.hasNext()) {
      val next = chars.next()
      if (next == '}') {
        val string = builder.toString()
        val title = string.substringBefore(':', "").let { if (it.isEmpty()) null else it }
        val label = string.substringAfter(':')
        return this.reference(label, title)
        }
      builder.append(next)
      }
    throw IllegalStructure("missing } in inline reference")
    }

  override fun toString() = nativeString()
  }

fun text(
    content: String? = null,
    format: Format = NORMAL,
    build: Text.() -> Unit = {}
    ) =
  Text(null, format).apply {
    content?.let { c ->
      readContent(c.iterator())
      }
    this.build()
    }

fun freeText(
    content: String? = null,
    format: Format = NORMAL,
    build: Text.() -> Unit = {}
    ) =
  Text(null, format).apply {
    content?.let { c ->
      readContent(c.iterator())
      }
    this.build()
    }

fun Inline.BaseContainer.text(
    content: String? = null,
    format: Format = NORMAL,
    build: Text.() -> Unit = { }
    ) =
  Text(this, format).also { t ->
    content?.let { c -> t.readContent(c.iterator()) }
    t.build()
    this.add(t)
  }

fun Block.BaseParent.text(content: String? = null, format: Format = NORMAL, build: Text.() -> Unit = {}) =
  Text(this, format).also { text ->
    content?.let { text.readContent(it.iterator()) }
    text.build()
    }

fun code(content: String) = Text(null, CODE).apply {
  add(Content(this, content))
  }

fun Inline.BaseContainer.code(content: String) = Text(this, CODE).also { text ->
  text.add(Content(text, content))
  this.add(text)
  }

class Content(context: Context?, val value: String) : Inline {
  override var context = context ?: FreeContext
  override fun isEmpty() = value.isEmpty()
  override fun nativeString(builder: StringBuilder) { builder.append(value) }
  override fun toString() = value
  }

fun Text.bold(content: String? = null, build: Text.() -> Unit = {}) = text(content, BOLD, build)
fun Text.italic(content: String? = null, build: Text.() -> Unit = {}) = text(content, ITALIC, build)
fun Text.underline(content: String? = null, build: Text.() -> Unit = {}) = text(content, UNDERLINE, build)
fun Text.strike(content: String? = null, build: Text.() -> Unit = {}) = text(content, STRIKE, build)

fun Inline.BaseContainer.bold(content: String? = null, build: Text.() -> Unit = {}) = text(content, BOLD, build)
fun Inline.BaseContainer.italic(content: String? = null, build: Text.() -> Unit = {}) = text(content, ITALIC, build)
fun Inline.BaseContainer.underline(content: String? = null, build: Text.() -> Unit = {}) = text(content, UNDERLINE, build)
fun Inline.BaseContainer.strike(content: String? = null, build: Text.() -> Unit = {}) = text(content, STRIKE, build)

fun String.toText() = text(this)
