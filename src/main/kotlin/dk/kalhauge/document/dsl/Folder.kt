package dk.kalhauge.document.dsl

class Folder(
    override val trunk: Folder?,
    override val name: String,
    override val label: String
    ) : Context {
  companion object {
    val targets = mutableMapOf<String, Target>()
    }
  override val branches = mutableListOf<Context>()
  override val path: String = if (trunk == null) name else "${trunk.path}/$name"
  private var resourcePath: String? = null
  override var resources: String
    get() = trunk?.resources ?: "$name/${resourcePath?:"resources"}"
    set(value) {
      if (trunk == null) resourcePath = value
      else trunk.resources = "$name/$value"
      }

  init {
    trunk?.add(this)
    }

  override fun add(context: Context) { branches += context }

  override fun add(label: String, target: Target) {
    if (trunk != null) trunk.add("${this.label}/$label", target)
    else targets[label] = target
    }

  override fun findTarget(label: String): Target? =
      if (trunk == null) targets[label]
      else trunk.findTarget("${this.label}/$label") ?: trunk.findTarget(label)

  }

fun path(name: String, label: String? = null, build: Folder.() -> Unit) =
    if (label == null) Folder(null, name, name).apply(build)
    else Folder(null, name, label).apply(build)

fun Folder.path(name: String, label: String? = null, build: Folder.() -> Unit) =
    if (label == null) Folder(this, name, name).apply(build)
    else Folder(this, name, label).apply(build)
