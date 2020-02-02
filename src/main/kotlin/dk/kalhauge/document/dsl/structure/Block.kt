package dk.kalhauge.document.dsl.structure

import dk.kalhauge.document.dsl.paragraph
import dk.kalhauge.document.dsl.text

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
    operator fun String.unaryPlus() {
      val content = this
      paragraph { text(content) }
      }
    }

  interface Child: Block {
    var context: Context
    fun isEmpty(): Boolean
    }

  }

