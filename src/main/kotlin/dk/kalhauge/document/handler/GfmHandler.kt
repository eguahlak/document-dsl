package dk.kalhauge.document.handler

import dk.kalhauge.document.dsl.*
import dk.kalhauge.document.dsl.Text.Format.*
import dk.kalhauge.util.*

class GfmHandler(val host: Host, val root: Context) {
  private val relations = Relations()

  fun handle(printTargets: Boolean = false, printRelations: Boolean = false) {
    relations.collectFrom(root)
    if (printTargets) {
      println("Targets:")
      Context.targets.forEach { (k, v) -> println("$k --> $v") }
      println();
      }
    if (printRelations) relations.print()
    when (root) {
      is Folder ->  handle(root)
      is Document -> handle(root)
      is RawDocument -> handle(root)
      else -> TODO("Handling for new context type: $root")
      }
    }

  private fun handle(folder: Folder) {
    println("Folder: ${folder.name}")
    folder.branches.forEach {
      when (it) {
        is Folder -> handle(it)
        is Document -> handle(it)
        is RawDocument -> handle(it)
        }
      }
    }

  private fun handle(children: List<Block.Child>, parent: Block.Parent) {
    children.forEach {
      when (it) {
        is Special -> handle(it)
        is Section -> handle(it)
        is Paragraph -> handle(it, parent)
        is Code -> handle(it)
        is Listing -> handle(it, parent)
        is Table -> handle(it)
        is TableOfContent -> { }
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

  private fun handle(file: RawDocument) = with(host) {
    open("${file.path}")
    printLine(file.content, 0)
    close()
    }

  private fun handle(section: Section) = with(host) {
    val (_, level, _, prefix) = relations.sections[section]!!
    val numbering = if (configuration.hasNumbers) "$prefix " else ""
    printLine(level of "#", "$numbering${evaluate(section.title)}" )
    handle(section.children, section)
    }

  private fun handle(special: Special) = with(host) {
    when (special) {
      is TableOfContent -> {
        printLine("${special.level of "#"} ${evaluate(special.title)}")
        handle(special.children, special)
        }
      }
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

  fun handle(table: Table) = with (host) {
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
        STRIKE -> "~~$this~~"
        CODE -> "`$this`"
        }

  val Reference.document: Document get() = relations.references[this]!!.document

  private fun sourceOf(reference: Reference, resource: Resource) =
    when (resource) {
      is CachedResource -> {
        when (resource.source) {
          is Address.Disk ->
            host.updateFile(resource.source.path, "${root.resources}/${resource.name}")
          is Address.Web ->
            host.downloadFile(resource.source.url, "${root.resources}/${resource.name}")
          }
        "${root.resources}/${resource.name}" from reference.document.path
        }
      else -> resource.source.url
      }

  fun emptyOf(format: Text.Format) = when (format) {
    ITALIC -> "/"
    STRIKE -> "~"
    else -> """\${format.delimiter}"""
    }

  fun evaluate(inline: Inline?): String =
      when (inline) {
        null -> ""
        is Content -> {
          inline.value.replace("\\{\\{([^{}]*:)?([^{}]*)\\}\\}".toRegex()) { match ->
            val pre = match.groupValues[1]
            val title = if (pre.isEmpty()) null else pre.substringBefore(":")
            val label = match.groupValues[2]
            when (val target = Context[label]) {
              is CachedResource -> {
                "${if (target.render) "!" else ""}[${title ?: evaluate(target.title)}](${target.source.url})"
                }
              is Resource -> {
                "[${title ?: evaluate(target.title)}](${target.source.url})"
                }
              else -> "<<Cannot inline: $target>>"
              }
            }
          }
        is Text ->
            if (inline.isEmpty()) emptyOf(inline.format)
            else inline.parts.joinToString("") { evaluate(it) } with inline.format
        is Reference -> {
          // val (source) = relations.references[inline] ?: throw IllegalStructure("No relations to: $inline")
          val source = inline.document
          //val fullLabel = normalizePath("${source.path}/${inline.label}")
          when (val target = inline.target.fixFrom(source)) { // TODO Check
            is Section -> {
              val (destination, _, _, prefix) = relations.sections[target]!!
              val title =
                  if (inline.title != null) evaluate(inline.title)
                  else if (host.configuration.hasNumbers) "$prefix ${evaluate(target.title)}"
                  else evaluate(target.title)
              val url =
                  if (host.configuration.hasNumbers) "$prefix ${evaluate(target.title)}"
                  else evaluate(target.title)
              "[$title](${(destination from source)-".md"}#${url.anchorize()})"
              }
            is CachedResource -> {
              "${if (target.render) "!" else ""}[${evaluate(inline.title ?: target.title)}](${sourceOf(inline, target)})"
              }
            is Resource -> {
              "[${evaluate(inline.title ?: target.title)}](${sourceOf(inline, target)})"
              }
            is Document -> "[${evaluate(inline.title ?: target.title)}](${target from source}.md)"
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