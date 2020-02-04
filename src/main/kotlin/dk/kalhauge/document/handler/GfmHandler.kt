package dk.kalhauge.document.handler

import dk.kalhauge.document.dsl.*
import dk.kalhauge.document.dsl.Text.Format.*
import dk.kalhauge.document.dsl.structure.*
import dk.kalhauge.util.*

class GfmHandler(private val host: Host, val root: Tree.Root) {

  fun handle(printTargets: Boolean = false) {
    if (printTargets) {
      println("Targets:")
      root.targets.forEach { println("${it.key} --> ${it.value}") }
      println()
      }
    root.branches.forEach {
      when (it) {
        is Folder -> handle(it)
        is Document -> handle(it)
        is FileDocument -> handle(it)
        else -> TODO("Handling for new context type: $it")
        }
      }
    }

  private fun handle(folder: Folder) {
    println("Folder: ${folder.name}")
    folder.branches.forEach {
      when (it) {
        is Folder -> handle(it)
        is Document -> handle(it)
        is FileDocument -> handle(it)
        }
      }
    }

  private fun handle(children: List<Block.Child>) {
    children.forEach {
      when (it) {
        is Special -> handle(it)
        is Section -> handle(it)
        is Capture -> handle(it)
        is Paragraph -> handle(it)
        is Code -> handle(it)
        is Listing -> handle(it)
        is Table -> handle(it)
        else -> TODO("Handling for new Block.Child type: $it")
        }
      }
    }

  private fun handle(document: Document) = with(host) {
    open("${document.path}.md")
    if (configuration.hasTitle) printLine("# **${evaluate(document.title)}**")
    handle(document.children)
    close()
    }

  private fun handle(file: FileDocument) = with(host) {
    open(file.path)
    printLine(file.content, 0)
    close()
    }

  private fun handle(section: Section) = with (host) {
    val prefix = if (configuration.hasNumbers) section.prefix else ""
    printLine(section.level of "#", "$prefix${evaluate(section.title)}" )
    handle(section.children)
    }

  private fun tocOf(parent: Block.Parent, indent: String): Unit = with (host) {
    parent.children.filterIsInstance<Section>().forEach {
      val title = "${if (configuration.hasNumbers) it.prefix else ""}${it.title}"
      printLine("$indent$title", 0)
      tocOf(it, "  $indent")
      }
    }


  private fun handle(special: Special): Unit = with (host) {
    when (special) {
      is TableOfContent -> {
        handle(special.generate())
        }
      else -> TODO("Implement special")
      }
    }

  private fun handle(paragraph: Paragraph) = with (host) {
    val parent = paragraph.context
    if (parent is Listing) {
      paragraph.parts.forEachIndexed { index, part ->
        if (index == 0) printLine("${parent markFor 0} ${evaluate(part)}", 0)
        else printLine("   ${evaluate(part)}", 0)
        }
      }
    else {
      paragraph.parts.forEach {
        if (paragraph.asQuote) printLine("> ${evaluate(it)}  ", 0)
        else printLine("${evaluate(it)}  ", 0)
        }
      printLine()
      }
    }

  private fun handle(capture: Capture) = with (host) {
    val parent = capture.context
    if (parent is Listing) {
      printLine("${parent markFor 0} ######", evaluate(capture.title), 0)
      capture.parts.forEachIndexed { index, part ->
        if (index == 0) printLine("   ${evaluate(part)}  ", 0)
        else printLine("   ${evaluate(part)}", 0)
        }
      }
    else {
      printLine("######", evaluate(capture.title), 0)
      capture.parts.forEach { printLine("${evaluate(it)}  ", 0) }
      printLine()
      }
    }

  private fun handle(code: Code) = with (host) {
    printLine("```${code.language}", 0)
    printLine(code.text, 0)
    printLine("```")
    }

  private fun handle(listing: Listing) = with (host) {
    val parent = listing.context
    if (parent is Listing) {
      indent += 2
      handle(listing.children)
      indent -= 2
      }
    else {
      handle(listing.children)
      printLine()
      }
    }

  private fun handle(table: Table) = with (host) {
    if (table.hideIfEmpty && table.rows.isEmpty()) return
    printLine(table.columns.joinToString(" | ", "| ", " |") { evaluate(it.title) }, 0)
    printLine(table.columns.joinToString(" | ", "| ", " |") { evaluate(it.alignment) }, 0)
    table.rows.forEach { row ->
      printLine(row.children.joinToString(" | ", "| ", " |") { evaluate(it) }, 0)
      }
    if (table.rows.isEmpty())
      printLine("|"+(table.columns.size of "  |" ), 0)
    printLine()
    }

  private infix fun Listing.markFor(index: Int) =
      when (this.type) {
        Listing.Type.BULLETED -> " *"
        Listing.Type.ARABIC -> " ${index + 1}."
        Listing.Type.ALPHABETIC -> " a."
        Listing.Type.ROMAN -> " i."
        }

  private infix fun String.with(format: Text.Format) =
      when (format) {
        NORMAL -> this
        BOLD -> "**$this**"
        ITALIC -> "_${this}_"
        UNDERLINE -> "<u>$this</u>"
        STRIKE -> "~~$this~~"
        CODE -> "`$this`"
        }

  private fun emptyOf(format: Text.Format) = when (format) {
    ITALIC -> "/"
    STRIKE -> "~"
    else -> """\${format.delimiter}"""
    }

  private fun evaluate(reference: Reference): String {
    val target = reference.target.fixate()
    val title =
        if (reference.title == null && target is Prefixed && host.configuration.hasNumbers)
            "${target.prefix}${evaluate(target.title)}"
        else evaluate(reference.title ?: target.title)
    val url =
        if (target is Prefixed && host.configuration.hasNumbers)
            "${target.prefix}${evaluate(target.title)}".anchorize()
        else evaluate(target.title).anchorize()

    val folderPath = reference.context.filePath.substringBeforeLast("/")
    when (target) {
      is Document -> {
        return "[$title](${target.filePath from folderPath}.md)"
        }
      is Section -> {
        return "[$title](${(target.filePath from folderPath) - ".md"}#${url.anchorize()})"
        }
      is Capture -> {
        return "[$title](${(target.filePath from folderPath) - ".md"}#${url.anchorize()})"
        }
      is CachedResource -> {
        val absolutePath = "${root.resources}/${target.name}"
        when (target.source) {
          is Address.Disk -> host.updateFile(target.source.path, absolutePath)
          is Address.Web -> host.downloadFile(target.source.url, absolutePath)
          }
        val relativePath = absolutePath from folderPath
        return "${if (target.render) "!" else ""}[$title]($relativePath)"
        }
      is Resource -> {
        return "[$title](${target.source.url})"
        }
      else -> return "[${target.title}]()"
      }
    }

  private fun evaluate(inline: Inline?): String =
      when (inline) {
        null -> ""
        is Content -> inline.value
        is Text ->
            if (inline.isEmpty()) emptyOf(inline.format)
            else inline.parts.joinToString("") { evaluate(it) } with inline.format
        is Reference -> evaluate(inline)
        else -> "<UNKNOWN inline: $inline>"
        }

  private fun evaluate(node: Block.Child) =
      when (node) {
        is Paragraph -> node.parts.joinToString(" ") { evaluate(it) }
        else -> "<CANNOT evaluate $node>"
      }

  private fun evaluate(alignment: HorizontalAlignment) =
      when (alignment) {
        HorizontalAlignment.LEFT ->    ":----"
        HorizontalAlignment.CENTER ->  ":---:"
        HorizontalAlignment.RIGHT ->   "----:"
        HorizontalAlignment.JUSTIFY -> "-----"
      }

  }