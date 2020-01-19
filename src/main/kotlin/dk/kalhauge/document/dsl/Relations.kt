package dk.kalhauge.document.dsl

class Relations {
  val sections = mutableMapOf<Section, Section.Relation>()
  val references = mutableMapOf<Reference, Reference.Relation>()
//  val targets = mutableMapOf<String, Target>()

  fun print() {
    println("Sections:")
    sections.forEach { t, u -> println("$t --> $u") }
    println()
    println("References:")
    references.forEach { t, u -> println("$t --> $u") }
    println()
    }

  fun collectFrom(context: Context) {
    when (context) {
      is Folder -> collectFrom(context)
      is Document -> collectFrom(context)
      }
    }

  private fun collectFrom(folder: Folder) {
    folder.branches.forEach {
      when (it) {
        is Folder -> collectFrom(it)
        is Document -> collectFrom(it)
        }
      }
    }

  private fun collectFrom(document: Document) {
    Context.targets["${document.path}/${document.label}"] = document
    var number = 1
    document.children.forEach {
      when (it) {
        is Section -> collectFrom(it, document, 1, number++, null)
        is Listing -> collectFrom(it, document)
        is Table -> collectFrom(it, document)
        is Inline.Parent -> collectFrom(it, document)
        }
      }
    }

  private fun collectFrom(section: Section, document: Document, level: Int, number: Int, prefix: String?) {
    val prefix = if (prefix == null) number.toString() else "$prefix.$number"
    sections[section] = Section.Relation(document, level, number, prefix)
    Context.targets["${document.path}/${section.label}"] = section
    var number = 1
    section.children.forEach {
      when (it) {
        is Section -> collectFrom(it, document, level + 1, number++, prefix)
        is Listing -> collectFrom(it, document)
        is Table -> collectFrom(it, document)
        is Inline.Parent -> collectFrom(it, document)
        }
      }
    }

  private fun collectFrom(listing: Listing, document: Document) {
    listing.children.forEach {
      when (it) {
        is Listing -> collectFrom(it, document)
        is Table -> collectFrom(it, document)
        is Inline.Parent -> collectFrom(it, document)
        }
      }
    }

  private fun collectFrom(table: Table, document: Document) {
    table.columns.forEach {
      collectFrom(it.title, document)
      }
    table.rows.flatMap { it.children }.forEach {
      when (it) {
        is Listing -> collectFrom(it, document)
        is Table -> collectFrom(it, document)
        is Inline.Parent -> collectFrom(it, document)
        }
      }
    }

  private fun collectFrom(parent: Inline.Parent, document: Document) {
    parent.parts.forEach {
      when (it) {
        is Inline.Parent -> collectFrom(it, document)
        is Reference -> collectFrom(it, document)
        is Resource -> collectFrom(it)
        }
      }
    }

  private fun collectFrom(reference: Reference, document: Document) {
    references[reference] = Reference.Relation(document)
    }

  private fun collectFrom(resource: Resource) {
    Context.targets[resource.label] = resource
    }

  }

