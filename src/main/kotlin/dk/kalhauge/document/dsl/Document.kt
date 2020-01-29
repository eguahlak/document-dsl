package dk.kalhauge.document.dsl

import dk.kalhauge.util.from


class Document(
    override val name: String,
    override val trunk: Context?,
    title: String?
    ): Block.Parent, Context, Target {
  override val label = "top"
  override val branches = emptyList<Context>()
  override val children = mutableListOf<Block.Child>()
  var title = text(title)
  private var resourcePath: String? = null
  override var resources: String
    get() = trunk?.resources ?: "$name/${resourcePath?:"resources"}"
    set(value) {
      if (trunk == null) resourcePath = value
      else trunk.resources = "$name/$value"
      }

  override fun add(child: Block.Child?) { if (child != null) children += child }

  override fun add(branch: Context) {
    throw IllegalStructure("No branches can be added to documents")
    }

  init {
    if (trunk == null) Context.root = this
    else trunk.add(this)
    }

  override fun toString() = """Document($path)"""

  fun postProcess() {
    children.filterIsInstance<Special>().forEach { it.process() }
    }

  }

fun document(
    name: String,
    title: String? = null,
    build: Document.() -> Unit
    ) =
  Document(name, null, title).also {
    it.build()
    it.postProcess()
    }

fun Folder.document(
    name: String,
    title: String? = null,
    build: Document.() -> Unit
    ) =
  Document(name, this, title).also {
    it.build()
    it.postProcess()
    }


infix fun Document.from(other: Document) =
    if (this == other) ""
    else this.path from other.path

class RawDocument(
    override val trunk: Context,
    override val name: String,
    content: String,
    val variables: Map<String, Any>
    ) : Context {
  val content: String
  init {
    this.content = content.replace("\\{\\{([^{}]*)\\}\\}".toRegex()) { match ->
      val result = variables[match.groupValues[1]] ?: "{{${match.groupValues[1]}?}}"
      result.toString()
      }
    trunk.add(this)
    }
  override val branches = emptyList<Context>()
  override var resources
    get() = trunk.resources
    set(value) { trunk.resources = value }
  override fun add(branch: Context) {
    throw IllegalStructure("No branches can be added to documents")
    }
  }

fun Folder.file(name: String, content: String, variables: Map<String, Any> = emptyMap()) =
  RawDocument(this, name, content, variables)
