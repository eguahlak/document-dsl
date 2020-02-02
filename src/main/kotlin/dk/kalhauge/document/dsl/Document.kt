package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Tree

class Document(
    override val trunk: Tree.Trunk,
    override val name: String,
    title: String?,
    label: String?
    ) : Block.BaseParent(), Tree.Branch, Target {
  val label = label ?: name

  override val keyPath = "${trunk.path}/${this.label}"
  override val filePath = "${trunk.path}/$name"
  //override val filePath = trunk.path
  override val key = keyPath
  override val path = filePath
  override fun register(target: Target) = root.register(target)
  override fun find(key: String) = root.find(key)

  override var title = text(title)
  override val root = trunk.root

  init {
    register()
    }

  override fun register() {
    this.root.register(this)
    }
  override fun toString() = "{/Document $name/:$path}"

  }

fun Tree.Trunk.document(
    name: String,
    title: String? = null,
    label: String? = null,
    build: Document.() -> Unit
    ) =
  Document(this, name, title, label).also {
    it.build()
    add(it)
    }

class FileDocument(
    override val trunk: Tree.Trunk,
    override val name: String,
    content: String,
    private val variables: Map<String, Any>
    ) : Tree.Branch {
  val content =
    content.replace("\\{\\{([^{}]*)}}".toRegex()) { match ->
      val result = variables[match.groupValues[1]] ?: "{{${match.groupValues[1]}?}}"
      result.toString()
      }
  }

fun Tree.Trunk.file(name: String, content: String, variables: Map<String, Any> = emptyMap()) =
  FileDocument(this, name, content, variables).also { add(it) }
