package dk.kalhauge.document.dsl

class Folder(
    override val name: String,
    override val trunk: Context?
    ) : Context {
  override val branches = mutableListOf<Context>()
  private var resourcePath: String? = null
  override var resources: String
    get() = trunk?.resources ?: "$name/${resourcePath?:"resources"}"
    set(value) {
      if (trunk == null) resourcePath = value
      else trunk.resources = "$name/$value"
      }

  init {
    if (trunk == null) Context.root = this
    else trunk.add(this)
    }

  override fun add(branch: Context) {
    branches += branch
    }
  }

fun folder(name: String, from: Folder? = null, build: Folder.() -> Unit) =
    Folder(name, from).also(build)

fun Folder.folder(name: String, build: Folder.() -> Unit) =
    Folder(name, this).also(build)
