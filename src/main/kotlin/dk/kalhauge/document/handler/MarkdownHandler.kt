package dk.kalhauge.document.handler

import dk.kalhauge.document.dsl.*
import dk.kalhauge.document.dsl.Target
import dk.kalhauge.document.dsl.Text.Format.*
import dk.kalhauge.util.anchorize
import dk.kalhauge.util.back
import dk.kalhauge.util.from
import dk.kalhauge.util.of

class MarkdownHandler(
    val host: Host,
    val configuration: Configuration = Configuration()
    ) {
  lateinit var document: Document

  fun handle(path: Folder) {
    path.branches.forEach {
      when (it) {
        is Document -> handle(it)
        is Folder -> handle(it)
        }
      }
    }

  fun handle(document: Document) {
    this.document = document
    with (host) {
      open("${document.path}.md")
      if (configuration.hasTitle) printLine("# **${evaluate(document.title)}**")
      document.children.forEach { handle(it) }
      close()
      }
    }

  fun handle(node: Block.Child) {
    with (host) {
      when (node) {
        is Section -> {
          printLine(node.level of "#", titleOf(node))
          node.children.forEach { handle(it) }
          }
        is Paragraph -> {
          if (node.parent is Listing) {
            node.parts.forEachIndexed { index, it ->
              val mark = if (index == 0) node.parent.type mark node.index else "  "
              printLine(mark, evaluate(it), 0)
              }
            }
          else {
            node.parts.forEach { printLine(evaluate(it), 0) }
            printLine()
            }
          }
        is Listing -> {
          if (node.parent is Listing) {
            indent += 2
            node.children.forEach { handle(it) }
            indent -= 2
            }
          else {
            node.children.forEach { handle(it) }
            printLine()
            }
          }
        is Table -> {
          printLine(node.columns.joinToString(" | ", "| ", " |") { evaluate(it.title) }, 0)
          printLine(node.columns.joinToString(" | ", "| ", " |") { evaluate(it.alignment) }, 0)
          node.rows.forEach { row ->
            printLine(row.children.map { evaluate(it) }.joinToString(" | ", "| ", " |"), 0)
            }
          printLine()
          }
        is Code -> {
          printLine("```${node.language}", 0)
          printLine(node.text, 0)
          printLine("```")
          }
        else -> {
          printLine(">>> UNKNOWN Node: $node")
          }
        }
      }
    }

  infix fun Listing.Type.mark(index: Int) =
    when (this) {
      Listing.Type.BULLETED -> " *"
      Listing.Type.ARABIC -> " ${index + 1}."
      Listing.Type.ALPHABETIC -> " a."
      Listing.Type.ROMAN -> " i."
      }

  infix fun String.with(format: Text.Format) =
    when (format) {
      NORMAL -> this
      BOLD -> "**$this**"
      ITALIC -> "_${this}_"
      UNDERLINE -> "<u>$this</u>"
      STRIKE -> "~$this~"
      CODE -> "`$this`"
      }

  fun titleOf(target: Target) = when (target) {
    is Section ->
      if (configuration.hasNumbers) "${target.number} ${evaluate(target.title)}"
      else evaluate(target.title)
    is Resource -> evaluate(target.title)
    else -> TODO("Implement $target as target")
    }

  fun sourceOf(target: Target) = when (target) {
    is Section ->
      if (target.document == document) "#${titleOf(target).anchorize()}"
      else "${target.document.path from document.path}.md#${titleOf(target).anchorize()}"
    is Resource -> {
      when (target.type) {
        Resource.Type.LINK -> {
          target.source.path
          }
        Resource.Type.DOCUMENT -> {
          when (target.source) {
            is Address.Disk -> {
              host.updateFile(target.source.path, "${document.resources}/${target.link!!}")
              }
            is Address.Web ->
              host.downloadFile(target.source.path, "${document.resources}/${target.link!!}")
            }
          "${document.resources}/${target.link}" from document.path
          }
        Resource.Type.IMAGE -> {
          when (target.source) {
            is Address.Disk ->
              host.updateFile(target.source.path, "${document.resources}/${target.link}")
            is Address.Web ->
              host.downloadFile(target.source.path, "${document.resources}/${target.link}")
            }
          "${document.resources}/${target.link}" from document.path
          }
        }
      }
    else -> TODO("Implement $target as target")
    }

  fun evaluate(inline: Inline?): String =
    when (inline) {
      null -> ""
      is Content -> inline.value
      is Text ->  if (inline.isEmpty()) """\${inline.format.delimiter}"""
                  else inline.parts.joinToString("") { evaluate(it) } with inline.format
      is Reference -> {
        val target = inline.target
        when (target) {
          null -> "<Illegal label: ${inline.label}>"
          is Section -> "[${titleOf(target)}](${sourceOf(target)})"
          is Resource -> {
            if (target.type == Resource.Type.IMAGE)
                "![${titleOf(target)}](${sourceOf(target)})"
            else "[${titleOf(target)}](${sourceOf(target)})"
            }
          else -> "<Unknown target: $target>"
          }
        }
      else -> "<UNKNOWN inline: $inline>"
      }

  fun evaluate(node: Block.Child) =
    when (node) {
      is Paragraph -> node.parts.joinToString(node.space) { evaluate(it) }
      else -> "<CANNOT evaluate $node>"
      }

  fun evaluate(alignment: HorizontalAlignment) =
    when (alignment) {
      HorizontalAlignment.LEFT ->    ":----"
      HorizontalAlignment.CENTER ->  ":---:"
      HorizontalAlignment.RIGHT ->   "----:"
      HorizontalAlignment.JUSTIFY -> "-----"
      }

  }


