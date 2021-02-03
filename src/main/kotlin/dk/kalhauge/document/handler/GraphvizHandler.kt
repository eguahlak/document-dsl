package dk.kalhauge.document.handler

import dk.kalhauge.document.dsl.graphs.*
import dk.kalhauge.util.Docker
import dk.kalhauge.util.id

class GraphvizHandler(val host: Host) : GraphHandler {

  override fun handle(graph: Graph) = with(host) {
    val filename = "${graph.context.filePath.substringBeforeLast("/")}/${graph.name}.gv"
    open(filename)
    printLine(
      """
      |digraph ${graph.cluster.title.id()} {
      |  charset = "UTF-8";
      |  label = "${graph.cluster.title}";
      |  rankdir = "${graph.cluster.direction}"
      """.trimMargin(), 0)
    handle(graph.cluster, "  ")
    printLine(
      """
      |  }  
      """.trimMargin())
    close()
    }

  override fun postProcess() {
    val repository = "eguahlak/graphvizzer:1.1.0"
    try {
      println("Preparing to process graphical files ...")
      Docker.pull(repository)?.container {
        //ports()
        mounts("${host.configuration.contextRoot}/docs" to "/graphs")
        }?.start().use { /* calls close on exit */ }
      println("*.gv files processed to *.png files")
      }
    catch (e: Exception) {
      println("Cannot start docker container with image $repository")
      }
    }

  fun handle(cluster: Cluster, indent: String): Unit = with(host) {
    if (cluster.isRoot()) {
      println("handle root cluster")
      cluster.vertices.forEach { handle(it, "$indent") }
      cluster.clusters.forEach { handle(it, "$indent") }
      cluster.vertices.forEach { vertice ->
        vertice.edges.forEach { edge -> handle(vertice, edge, "$indent") }
        }
      }
    else {
      printLine(
        """
        |
        |${indent}subgraph cluster${cluster.title.id()} {
        |${indent}  label = "${cluster.title}";
        """.trimMargin(), 0)
      cluster.vertices.forEach { handle(it, "$indent  ") }
      cluster.clusters.forEach { handle(it, "$indent  ") }
      cluster.vertices.forEach { vertice ->
        vertice.edges.forEach { edge -> handle(vertice, edge, "$indent  ") }
        }
      printLine("""$indent  }""", 0)
      }
    }



  fun handle(vertex: Vertex, indent: String) = with (host) {
    val arguments = listOf(labelOf(vertex), shapeOf(vertex),styleOf(vertex),colorsOf(vertex))
    printLine(
      """${indent}${vertex.title.id()} ${arguments.joinToString(",", "[", "]")};""",
      0)
    // TODO add style, color and fill
    }

  fun handle(source: Vertex, edge: Edge, indent: String) = with(host) {
    val arguments = listOf(styleOf(edge), shapeOf(edge), colorsOf(edge))
    printLine(
      """${indent}${source.title.id()} -> ${edge.target.title.id()} ${arguments.joinToString(",", "[", "]")};""",
      0)
    }

  fun colorsOf(vertex: Vertex): String = with(vertex) {
    """color="${color}""""+
    (if (fill != RGB.INVISIBLE) """,fillcolor="${fill.light}"""" else "")
    }

  fun colorsOf(edge: Edge): String = with(edge) {
    """color="${color}""""
    }

  fun styleOf(vertex: Vertex) = with(vertex) {
    if (fill == RGB.INVISIBLE) "style=${evaluate(style)}"
    else """style="${evaluate(style)},filled""""
    }

  fun styleOf(edge: Edge) = "style=${evaluate(edge.style)}"

  fun labelOf(vertex: Vertex) = """label="${vertex.title}""""

  fun shapeOf(vertex: Vertex) = """shape=${evaluate(vertex.shape)}"""

  fun shapeOf(edge: Edge) = """arrowhead=${evaluate(edge.arrowHead)}"""

  fun evaluate(style: Cluster.Style) =
      when(style) {
        Cluster.Style.DASHED -> "dashed"
        Cluster.Style.DOTTED -> "dottet"
        Cluster.Style.SOLID -> "solid"
        }

  fun evaluate(shape: Vertex.Shape) =
      when (shape) {
        Vertex.Shape.BOX -> "box"
        Vertex.Shape.ELLIPSE -> "ellipse"
        Vertex.Shape.CIRCLE -> "circle"
        }

  fun evaluate(arrowHead: Edge.ArrowHead) =
      when (arrowHead) {
        Edge.ArrowHead.TRIANGLE -> "normal"
        Edge.ArrowHead.OPEN_TRIANGLE -> "onormal"
        Edge.ArrowHead.WEE -> "vee"
        Edge.ArrowHead.NONE -> "none"
        }

  }