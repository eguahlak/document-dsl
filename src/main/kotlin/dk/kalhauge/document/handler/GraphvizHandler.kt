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
    val arguments = with (vertex) { listOf(evaluate(title),evaluate(shape),evaluate(style),colors(this)) }
    printLine(
      """${indent}${vertex.title.id()} ${arguments.joinToString(",", "[", "]")};""",
      0)
    // TODO add style, color and fill
    }

  fun handle(source: Vertex, edge: Edge, indent: String) = with(host) {
    val arguments = with (edge) { listOf(evaluate(style), evaluate(arrowHead), colors(this)) }
    printLine(
      """${indent}${source.title.id()} -> ${edge.target.title.id()} ${arguments.joinToString(",", "[", "]")};""",
      0)
    }

  fun colors(vertex: Vertex): String = with(vertex) {
    """color="${color}""""+
    (if (fill != RGB.INVISIBLE) """,style=filled,fillcolor="${fill.light}"""" else "")
    }

  fun colors(edge: Edge): String = with(edge) {
    """color="${color}""""
    }

  fun evaluate(label: String) = """label="$label""""

  fun evaluate(style: Cluster.Style) =
      when(style) {
        Cluster.Style.DASHED -> "style=dashed"
        Cluster.Style.DOTTED -> "style=dottet"
        Cluster.Style.SOLID -> "style=solid"
        }

  fun evaluate(shape: Vertex.Shape) =
      when (shape) {
        Vertex.Shape.BOX -> "shape=box"
        Vertex.Shape.ELLIPSE -> "shape=ellipse"
        Vertex.Shape.CIRCLE -> "shape=circle"
        }

  fun evaluate(arrowHead: Edge.ArrowHead) =
      when (arrowHead) {
        Edge.ArrowHead.TRIANGLE -> "arrowhead=normal"
        Edge.ArrowHead.OPEN_TRIANGLE -> "arrowhead=onormal"
        Edge.ArrowHead.WEE -> "arrowhead=vee"
        Edge.ArrowHead.NONE -> "arrowhead=none"
        }

  }