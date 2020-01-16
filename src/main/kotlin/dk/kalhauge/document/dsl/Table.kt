package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.HorizontalAlignment.*
import dk.kalhauge.document.dsl.VerticalAlignment.*

interface Figure {
  val capture: Text?
  }

enum class HorizontalAlignment { LEFT, CENTER, RIGHT, JUSTIFY }

enum class VerticalAlignment { TOP, MIDDLE, BOTTOM }

class Table(parent: Block.Parent, override val capture: Text? = null) : Block.Child(parent), Figure {
  val columns = mutableListOf<Column>()
  val rows = mutableListOf<Row>()

  init {
    parent.add(this)
    }

  fun column(title: String? = null, alignment: HorizontalAlignment = LEFT, build: Column.() -> Unit = { }) =
    Column(this, alignment).also { c ->
      title?.let { c.title(title) }
      c.build()
      }

  fun left(title: String? = null, build: Column.() -> Unit = {}) =
    column(title, LEFT, build)

  fun center(title: String? = null, build: Column.() -> Unit = {}) =
    column(title, CENTER, build)

  fun right(title: String? = null, build: Column.() -> Unit = {}) =
    column(title, RIGHT, build)

  fun justify(title: String? = null, build: Column.() -> Unit = {}) =
    column(title, JUSTIFY, build)

  fun row(alignment: VerticalAlignment = TOP, build: Row.() -> Unit = {}) =
    Row(this, alignment).also { it.build() }

  override fun print(indent: String) {
    println("${indent}Table")
    rows.forEach { it.print("$indent  ") }
    }

  class Column(val table: Table, val alignment: HorizontalAlignment) {
    var index = table.columns.size
    var title: Text? = null

    fun title(content: String? = null, build: Text.() -> Unit = { }) {
      title = text(content, build = build)
      }

    init {
      table.columns += this
      }

    fun print(indent: String) {
      println("${indent}Column: $alignment")
      }

    }

  class Row(val table: Table, val alignment: VerticalAlignment) : Block.Parent {
    val index = table.rows.size
    override val children = mutableListOf<Block.Child>()
    override val level = table.parent.level + 1
    override val document = table.document

    init {
      table.rows += this
      }

    override fun add(child: Block.Child) { children += child }

    override fun print(indent: String) {
      println("${indent}Row:")
      children.forEach { it.print("$indent  ") }
      }
    }

  }

fun Block.Parent.table(build: Table.() -> Unit = { }) = Table(this).also(build)
