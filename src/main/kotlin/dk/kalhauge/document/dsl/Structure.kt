package dk.kalhauge.document.dsl

interface Context {
  val trunk: Context?
  val branches: List<Context>
  val name: String
  val path: String get() = if (trunk == null) name else "${trunk?.path}/$name"
  var resources: String
  companion object {
    val targets = mutableMapOf<String, Target>()
    operator fun get(label: String): Target {
      val target = targets[label]
      if (target == null) {
        println("Trying to find: $label")
        println("Searching...")
        targets.filter { it.key.endsWith(label) }.forEach { (k, v) -> println("$k --> $v") }
        throw IllegalStructure("Cant find target")
        }
      else return target
      }
    lateinit var root: Context
    }
  fun add(branch: Context)
  }

interface Block {

  interface Parent: Block {
    val children: List<Child>
    fun add(child: Child)
    operator fun String.unaryPlus() {
      val content = this
      paragraph { text(content) }
      }
    }

  interface Child: Block

  }

interface Inline {
  fun isEmpty(): Boolean
  fun nativeString(builder: StringBuilder)
  fun nativeString() = buildString { nativeString(this) }

  interface Parent {
    val parts: List<Inline>
    fun add(part: Inline)
    operator fun String.unaryPlus() {
      val content = this
      text(content)
      }
    }

  }

class IllegalStructure(message: String): RuntimeException(message)