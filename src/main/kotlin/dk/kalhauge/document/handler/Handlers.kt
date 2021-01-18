package dk.kalhauge.document.handler

import dk.kalhauge.document.dsl.graphs.Graph

interface GraphHandler {
  fun handle(graph: Graph)
  }