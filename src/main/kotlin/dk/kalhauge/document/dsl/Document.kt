package dk.kalhauge.document.dsl


class Document(
    override val trunk: Folder?,
    override val name: String,
    override val label: String,
    title: String?
    ) : Block.Parent, Context {
  override val branches = emptyList<Context>()
  override val level = 0
  override val children = mutableListOf<Block.Child>()
  override val document = this
  val title = if (title == null) text(name.toLowerCase().capitalize()) else text(title)
  override val path = if (trunk == null) name else "${trunk.path}/$name"
  val targets = mutableListOf<Target>()
  private var resourcePath: String? = null
  override var resources: String
    get() = trunk?.resources ?: "$name/${resourcePath?:"resources"}"
    set(value) {
      if (trunk == null) resourcePath = value
      else trunk.resources = "$name/$value"
    }

  val here = this

  override fun findTarget(label: String) =
    if (trunk == null) Folder.targets[label]
    else trunk.findTarget("${this.label}/$label") ?: trunk.findTarget(label)

/*
  SMALL/sec:1
 */

  init {
    trunk?.add(this)
    }

  override fun add(label: String, target: Target) {
    if (trunk != null) trunk.add("${this.label}/$label", target)
    else Folder.targets[label] = target
    }

  override fun add(child: Block.Child) {
    if (child is Target) {
      targets += child
      add(child.label, child)
      }
    children += child
    }

  override fun add(context: Context) { throw IllegalStructure() }

  }

fun document(name: String, title: String? = null, label: String? = null, build: Document.() -> Unit) =
    if (label == null) Document(null, name, name, title).also(build)
    else Document(null, name, label, title).also(build)

fun Folder.document(name: String, title: String? = null, label: String? = null, build: Document.() -> Unit) =
  if (label == null) Document(this, name, name, title).also(build)
  else Document(this, name, label, title).also(build)
