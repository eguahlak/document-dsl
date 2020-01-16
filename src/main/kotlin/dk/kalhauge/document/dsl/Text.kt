package dk.kalhauge.document.dsl

import java.lang.UnsupportedOperationException
import dk.kalhauge.document.dsl.Text.*
import dk.kalhauge.document.dsl.Text.Format.*


const val EOT = '\u0000'
const val delimiters = "$EOT*/_~`"

class Text(override val document: Document?, val format: Format = NORMAL) : Inline, Inline.Parent {

  override val parts = mutableListOf<Inline>()

  override fun add(part: Inline) { parts += part }

  enum class Format(val delimiter: Char) {
    NORMAL(EOT), BOLD('*'), ITALIC('/'), UNDERLINE('_'), STRIKE('~'), CODE('`')
    }

  fun isEmpty() = parts.isEmpty() || parts[0].nativeString().isEmpty()

  override fun nativeString(builder: StringBuilder) {
    if (format != NORMAL) builder.append(format.delimiter)
    parts.forEach { it.nativeString(builder) }
    if (format != NORMAL) builder.append(format.delimiter)
    }

  override fun nakedString(builder: StringBuilder) {
    parts.forEach { it.nakedString(builder) }
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
    if (builder.length > 0) add(Content(builder.toString()))
    when (delimiter) {
      format.delimiter -> return
      EOT -> return
      '*' -> Text(document, BOLD).also { target.add(it) }.readContent(chars)
      '/' -> Text(document, ITALIC).also { target.add(it) }.readContent(chars)
      '_' -> Text(document, UNDERLINE).also { target.add(it) }.readContent(chars)
      '~' -> Text(document, STRIKE).also { target.add(it) }.readContent(chars)
      '`' -> Text(document, STRIKE).also { target.add(it) }.readContent(chars)
      else -> UnsupportedOperationException("Unknown $delimiter")
      }
    readContent(chars)
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

fun Block.Parent.text(
    content: String? = null,
    format: Format = NORMAL,
    build: Text.() -> Unit = {}) =
  Text(document, format).apply {
    content?.let { c ->
      readContent(c.iterator())
      }
    this.build()
    }

fun Inline.Parent.text(
  content: String? = null,
  format: Text.Format = Text.Format.NORMAL,
  build: Text.() -> Unit = { }) =
  Text(document, format).also { t ->
    content?.let { c -> t.readContent(c.iterator()) }
    t.build()
    this.add(t)
    }

fun code(content: String) = Text(null, CODE).apply {
  add(Content(content))
  }

fun Inline.Parent.code(content: String) = Text(document, CODE).also {
  it.add(Content(content))
  this.add(it)
  }

class Content(val value: String) : Inline {
  override val document = null
  override fun nakedString(builder: StringBuilder) { builder.append(value) }
  override fun nativeString(builder: StringBuilder) { builder.append(value) }
  override fun toString() = value
  }

fun Text.bold(content: String? = null, build: Text.() -> Unit = {}) = text(content, BOLD, build)
fun Text.italic(content: String? = null, build: Text.() -> Unit = {}) = text(content, ITALIC, build)
fun Text.underline(content: String? = null, build: Text.() -> Unit = {}) = text(content, UNDERLINE, build)
fun Text.strike(content: String? = null, build: Text.() -> Unit = {}) = text(content, STRIKE, build)


