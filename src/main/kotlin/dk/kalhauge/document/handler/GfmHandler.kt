package dk.kalhauge.document.handler

import dk.kalhauge.document.dsl.*
import dk.kalhauge.document.dsl.Text.Format.*
import dk.kalhauge.util.anchorize
import dk.kalhauge.util.of
import dk.kalhauge.util.from
import dk.kalhauge.util.normalizePath

class GfmHandler(val host: Host, val configuration: Configuration, val root: Context) {
  val relations = Relations()

  fun handle() {
    relations.collectFrom(root)
    when (root) {
      is Folder ->  handle(root)
      is Document -> handle(root)
      else -> TODO("Handling for new context type: $root")
      }
    }

  private fun handle(folder: Folder) {
    println("Folder: ${folder.name}")
    folder.branches.forEach {
      when (it) {
        is Folder -> handle(it)
        is Document -> handle(it)
        }
      }
    }

  private fun handle(children: List<Block.Child>, parent: Block.Parent) {
    children.forEach {
      when (it) {
        is Section -> handle(it)
        is Paragraph -> handle(it, parent)
        is Code -> handle(it)
        is Listing -> handle(it, parent)
        is Table -> handle(it, parent)
        else -> TODO("Handling for new Block.Child type: $it")
        }
      }
    }

  private fun handle(document: Document) = with(host) {
    open("${document.path}.md")
    if (configuration.hasTitle) printLine("# **${evaluate(document.title)}**")
    handle(document.children, document)
    close()
    }

  private fun handle(section: Section) = with(host) {
    val (document, level, number, prefix) = relations.sections[section]!!
    val numbering = if (configuration.hasNumbers) "$prefix " else ""
    printLine(level of "#", "$numbering${section.title.nativeString()}" )
    handle(section.children, section)
    }

  private fun handle(paragraph: Paragraph, parent: Block.Parent) = with (host) {
    if (parent is Listing) {
      paragraph.parts.forEachIndexed { index, part ->
        if (index == 0) printLine("${parent markFor 0} ${evaluate(part)}", 0)
        else printLine("   ${evaluate(part)}", 0)
        }
      }
    else {
      paragraph.parts.forEach {
        printLine("${evaluate(it)}  ", 0)
        }
      printLine()
      }
    }

  private fun handle(code: Code) = with (host) {
    printLine("```${code.language}", 0)
    printLine(code.text, 0)
    printLine("```")
    }

  fun handle(listing: Listing, parent: Block.Parent) = with (host) {
    if (parent is Listing) {
      indent += 2
      handle(listing.children, listing)
      indent -= 2
      }
    else {
      handle(listing.children, listing)
      printLine()
      }
    }

  fun handle(table: Table, parent: Block.Parent) = with (host) {
    printLine(table.columns.joinToString(" | ", "| ", " |") { evaluate(it.title) }, 0)
    printLine(table.columns.joinToString(" | ", "| ", " |") { evaluate(it.alignment) }, 0)
    table.rows.forEach { row ->
      printLine(row.children.map { evaluate(it) }.joinToString(" | ", "| ", " |"), 0)
      }
    printLine()
    }

  infix fun Listing.markFor(index: Int) =
      when (this.type) {
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

  val Reference.document: Document get() = relations.references[this]!!.document

  fun sourceOf(reference: Reference, resource: Resource) =
    when (resource.type) {
      Resource.Type.LINK -> resource.source.path
      Resource.Type.DOCUMENT -> {
        when (resource.source) {
          is Address.Disk ->
              host.updateFile(resource.source.path, "${root.resources}/${resource.link!!}")
          is Address.Web ->
              host.downloadFile(resource.source.path, "${root.resources}/${resource.link!!}")
          }
        "${root.resources}/${resource.link}" from reference.document.path
        }
      Resource.Type.IMAGE -> {
        when (resource.source) {
          is Address.Disk ->
            host.updateFile(resource.source.path, "${root.resources}/${resource.link}")
          is Address.Web ->
            host.downloadFile(resource.source.path, "${root.resources}/${resource.link}")
          }
        "${root.resources}/${resource.link}" from reference.document.path
        }
      }

  fun evaluate(inline: Inline?): String =
      when (inline) {
        null -> ""
        is Content -> inline.value
        is Text ->  if (inline.isEmpty()) """\${inline.format.delimiter}"""
        else inline.parts.joinToString("") { evaluate(it) } with inline.format
        is Reference -> {
          val (source) = relations.references[inline]!!
          val fullLabel = normalizePath("${source.path}/${inline.label}")
          val target = inline.target ?: Context.targets[fullLabel]
          when (target) {
            null -> "<Illegal label: ${inline.label}>"
            is Section -> {
              val (destination, level, number, prefix) = relations.sections[target]!!
              val title = if (configuration.hasNumbers) "$prefix ${evaluate(target.title)}"
                          else evaluate(target.title)
              "[$title](${destination from source}#${title.anchorize()})"
              }
            is Resource -> {
              if (target.type == Resource.Type.IMAGE)
                "![${evaluate(target.title)}](${sourceOf(inline, target)})"
              else "[${evaluate(target.title)}](${sourceOf(inline, target)})"
              }
            is Document -> "[${evaluate(target.title)}](${target from source}/${target.name}.md)"
            else -> "<Unknown target: $target>"
            }
          }
        else -> "<UNKNOWN inline: $inline>"
        }

  fun evaluate(node: Block.Child) =
      when (node) {
        is Paragraph -> node.parts.joinToString(" ") { evaluate(it) }
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