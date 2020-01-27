package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.HorizontalAlignment.*
import dk.kalhauge.document.dsl.VerticalAlignment.*

enum class HorizontalAlignment { LEFT, CENTER, RIGHT, JUSTIFY }

enum class VerticalAlignment { TOP, MIDDLE, BOTTOM }

class Table: Block.Child {
  val columns = mutableListOf<Column>()
  val rows = mutableListOf<Row>()

  override fun isEmpty() = columns.isEmpty() && rows.isEmpty()

  fun column(title: String?, alignment: HorizontalAlignment, build: Column.() -> Unit = { }) =
      Column(this, title, alignment).also(build)

  fun left(title: String? = null, build: Column.() -> Unit = {}) =
      column(title, LEFT, build)

  fun center(title: String? = null, build: Column.() -> Unit = {}) =
      column(title, CENTER, build)

  fun right(title: String? = null, build: Column.() -> Unit = {}) =
      column(title, RIGHT, build)

  fun justify(title: String? = null, build: Column.() -> Unit = {}) =
      column(title, JUSTIFY, build)

  fun row(alignment: VerticalAlignment = TOP, build: Row.() -> Unit = {}) =
      Row(this, alignment).also(build)

  class Column(val table: Table, title: String?, val alignment: HorizontalAlignment) {
    var index = table.columns.size
    var title = text(title)

    init { table.columns += this }

    }

  class Row(val table: Table, val alignment: VerticalAlignment) : Block.Parent {
    val index = table.rows.size
    override val children = mutableListOf<Block.Child>()

    init { table.rows += this }

    override fun add(child: Block.Child?) { if (child != null) children += child }


    }

  }

fun Block.Parent.table(build: Table.() -> Unit = { }) =
    Table().also {
      it.build()
      add(it)
      }
