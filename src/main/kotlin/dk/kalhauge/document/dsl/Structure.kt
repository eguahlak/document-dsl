package dk.kalhauge.document.dsl

interface Context: Target {
  val name: String
  val trunk: Context?
  val branches: List<Context>
  val path: String
  var resources: String

  fun findTarget(label: String): Target?
  fun add(context: Context)
  fun add(label: String, target: Target)
  }

interface Block {
  val level: Int
  val document: Document

  fun print(indent: String = "")

  interface Parent : Block {
    val children: List<Block.Child>

    fun add(child: Block.Child)

    override fun print(indent: String) {
      children.forEach { it.print("$indent  ") }
      }

    }

  abstract class Child(val parent: Parent) : Block {
    override val level = parent.level + 1
    val index = parent.children.size
    override val document: Document
      get() = parent.document
    }

  }

interface Inline {
  val document: Document?
  fun nativeString(builder: StringBuilder)
  fun nativeString() = buildString { nativeString(this) }

  fun nakedString(builder: StringBuilder)
  fun nakedString() = buildString { nakedString(this) }

  interface Parent {
    val document: Document?
    val parts: MutableList<Inline>
    fun add(part: Inline)
    }
  }

class IllegalStructure: RuntimeException()