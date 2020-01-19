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
  override fun add(child: Block.Child) { children += child }
  private var resourcePath: String? = null
  override var resources: String
    get() = trunk?.resources ?: "$name/${resourcePath?:"resources"}"
    set(value) {
      if (trunk == null) resourcePath = value
      else trunk.resources = "$name/$value"
      }

  override fun add(branch: Context) {
    throw IllegalStructure()
    }

  init {
    if (trunk == null) Context.root = this
    else trunk.add(this)
    }
  }

fun document(
    name: String,
    title: String? = null,
    build: Document.() -> Unit
    ) = Document(name, null, title).also(build)

fun Folder.document(
    name: String,
    title: String? = null,
    build: Document.() -> Unit
    ) = Document(name, this, title).also(build)

infix fun Document.from(other: Document) =
    if (this == other) ""
    else this.path from other.path