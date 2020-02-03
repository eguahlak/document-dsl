package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.HorizontalAlignment.*
import dk.kalhauge.document.dsl.VerticalAlignment.*
import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext

enum class HorizontalAlignment { LEFT, CENTER, RIGHT, JUSTIFY }

enum class VerticalAlignment { TOP, MIDDLE, BOTTOM }

class Table(context: Context?): Block.Child {
  var hideIfEmpty = true
  override var context = context ?: FreeContext
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
    val index = table.columns.size
    var title = text(title)

    init { table.columns += this }

    }

  class Row(val table: Table, val alignment: VerticalAlignment) : Block.BaseParent() {
    override val filePath get() = table.context.filePath
    override val keyPath get() = table.context.keyPath
    override fun register(target: Target) = table.context.register(target)
    override fun find(key: String) = table.context.find(key)

    val index = table.rows.size

    init { table.rows += this }

    }

  }

fun Block.BaseParent.table(build: Table.() -> Unit = { }) =
  Table(this).also {
    it.build()
    add(it)
    }

fun table(build: Table.() -> Unit = { }) =
  Table(null).also(build)
