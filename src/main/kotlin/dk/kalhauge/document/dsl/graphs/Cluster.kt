package dk.kalhauge.document.dsl.graphs

import dk.kalhauge.document.dsl.structure.Block
import dk.kalhauge.document.dsl.structure.Context
import dk.kalhauge.document.dsl.structure.FreeContext
import dk.kalhauge.util.hex


class RGB(val red: Int, val green: Int, val blue: Int, val alpha: Int = 255) {
  companion object {
    val RED = RGB(255, 0, 0)
    val GREEB = RGB(0, 255, 0)
    val BLUE = RGB(0, 0, 255)
    val GRAY = RGB(85, 85, 85)
    val BLACK = RGB(0, 0, 0)
    val INVISIBLE = RGB(0, 0, 0, 0)
    val WHITE = RGB(255, 255, 255)
    }

  override fun toString() = "#${red.hex}${green.hex}${blue.hex}${alpha.hex}"
  }

class Edge(
  val target: Vertex,
  val style: Cluster.Style = Cluster.Style.SOLID,
  val color: RGB = RGB.BLACK,
  val arrowHead: ArrowHead = ArrowHead.WEE
    ) {
  enum class ArrowHead { TRIANGLE, OPEN_TRIANGLE, WEE, NONE }
  }

class Vertex(
  val title: String,
  val shape: Shape = Shape.ELLIPSE,
  var style: Cluster.Style = Cluster.Style.SOLID,
  var color: RGB = RGB.BLACK,
  var fill: RGB = RGB.INVISIBLE
    ) {
  enum class Shape { BOX, ELLIPSE, CIRCLE }

  val edges = mutableListOf<Edge>()

  fun edge(
      target: Vertex?,
      style: Cluster.Style = Cluster.Style.SOLID,
      color: RGB = RGB.BLACK,
      arrowHead: Edge.ArrowHead = Edge.ArrowHead.WEE,
      build: Edge.() -> Unit = {}
      ) =
    if (target != null)
        Edge(target, style, color, arrowHead).also { edge ->
          edge.build()
          edges.add(edge)
          }
    else null
    }


interface SubGraph {

  }

class Graph(context: Context?, title: String, val name: String, val label: String): Block.Child, SubGraph {
  override var context = context ?: FreeContext
  override fun isEmpty() = cluster.isEmpty()
  val cluster = Cluster(this, title)
  }

class Cluster(val parent: SubGraph, val title: String): SubGraph {
  fun isEmpty() = vertices.isEmpty() && clusters.isEmpty()
  fun isRoot() = parent is Graph
  var direction = "TB"

  val clusters = mutableListOf<Cluster>()
  val vertices = mutableListOf<Vertex>()

  operator fun get(vertexTitle: String): Vertex? = vertices.find { it.title == vertexTitle }

  fun vertex(
      title: String,
      shape: Vertex.Shape = Vertex.Shape.ELLIPSE,
      style: Style = Style.SOLID,
      color: RGB = RGB.BLACK,
      fill: RGB = RGB.INVISIBLE,
      build: Vertex.() -> Unit = {}
      ) = Vertex(title, shape, style, color, fill).also { vertex ->
    vertex.build()
    vertices.add(vertex)
    }

  fun box(
      title: String,
      style: Style = Style.SOLID,
      color: RGB = RGB.BLACK,
      fill: RGB = RGB.INVISIBLE,
      build: Vertex.() -> Unit = {}
      ) = vertex(title, Vertex.Shape.BOX, style, color, fill, build)

  fun ellipse(
      title: String,
      style: Style = Style.SOLID,
      color: RGB = RGB.BLACK,
      fill: RGB = RGB.INVISIBLE,
      build: Vertex.() -> Unit = {}
      ) = vertex(title, Vertex.Shape.ELLIPSE, style, color, fill, build)

  fun circle(
      title: String,
      style: Style = Style.SOLID,
      color: RGB = RGB.BLACK,
      fill: RGB = RGB.INVISIBLE,
      build: Vertex.() -> Unit = {}
      ) = vertex(title, Vertex.Shape.CIRCLE, style, color, fill, build)

  enum class Style { SOLID, DASHED, DOTTED }

  fun cluster(title: String, build: Cluster.() -> Unit = {}) =
    Cluster(this, title).also { cluster ->
      cluster.build()
      clusters.add(cluster)
      }

  }

fun Block.BaseParent.graph(title: String, name: String, label: String, build: Cluster.() -> Unit = {}) =
    Graph(this, title, name, label).also { graph ->
      graph.cluster.build()
      add(graph)
      }

/*
                Cluster

         Graph 1 --- * Vertice 1 --- * Edge


 */