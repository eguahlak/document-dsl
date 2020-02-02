package dk.kalhauge.document.dsl.structure

interface Block {

  interface Parent: Block {
    val children: List<Child>
    fun add(child: Child?)
    }

  abstract class BaseParent : Parent, Context {
    override val children = mutableListOf<Child>()
    override fun add(child: Child?) {
      if (child == null || child.isEmpty()) return
      children += child
      child.context = this
      }
    }

  interface Child: Block {
    var context: Context
    fun isEmpty(): Boolean
    }

  }

/*
    operator fun String.unaryPlus() {
      val content = this
      paragraph { text(content) }
      }
*/