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
  val rows = mutableListOf<RowData>()

  fun csv(filename: String, skipLineCount: Int = 0) {
    rows += FileRows(filename, skipLineCount)
    }

  fun criterion(columnIndex: Int, predicate: (String) -> Boolean) {
    if (0 <= columnIndex && columnIndex < columns.size)
        columns[columnIndex].criterion = predicate
    }

  fun criterion(columnName: String, predicate: (String) -> Boolean) {
    criterion(columns.indexOfFirst { it.name == columnName }, predicate)
    }

  override fun isEmpty() = columns.isEmpty() && rows.isEmpty()

  fun column(
      title: String,
      name: String? = null,
      alignment: HorizontalAlignment,
      convert: (String) -> String = { it }
      ) = Column(this, title, name, alignment, convert)

  fun left(title: String, name: String? = null, convert: (String) -> String = { it }) =
      column(title, name, LEFT, convert)

  fun center(title: String, name: String? = null, convert: (String) -> String = { it }) =
      column(title, name, CENTER, convert)

  fun right(title: String, name: String? = null, convert: (String) -> String = { it }) =
      column(title, name, RIGHT, convert)

  fun justify(title: String, name: String? = null, convert: (String) -> String = { it }) =
      column(title, name, JUSTIFY, convert)

  fun row(alignment: VerticalAlignment = TOP, build: Row.() -> Unit = {}) =
    Row(this, alignment).also(build)

  class Column(
      private val table: Table,
      title: String,
      name: String?,
      val alignment: HorizontalAlignment,
      val convert: (String) -> String
      ) {
    val index = table.columns.size
    val name = name ?: title
    var title = text(title)
    var criterion: (String) -> Boolean = { true }

    init {
      table.columns += this
      }
    }

  interface RowData

  class Row(val table: Table, val alignment: VerticalAlignment) : Block.BaseParent(), RowData {
    override val filePath get() = table.context.filePath
    override val keyPath get() = table.context.keyPath
    override fun register(target: Target) = table.context.register(target)
    override fun find(key: String) = table.context.find(key)

    val index = table.rows.size

    init { table.rows += this }

    }

  class FileRows(val filename: String, val skipLineCount: Int = 0): RowData

  }



fun Block.BaseParent.table(build: Table.() -> Unit = { }) =
  Table(this).also {
    it.build()
    add(it)
    }

fun table(build: Table.() -> Unit = { }) =
  Table(null).also(build)
