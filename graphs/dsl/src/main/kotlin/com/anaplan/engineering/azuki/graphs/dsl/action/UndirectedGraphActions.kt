package com.anaplan.engineering.azuki.graphs.dsl.action

interface UndirectedGraphActions : UndirectedGraphDeclarableActions {

    fun createUndirected(graphName: String)

}

interface DirectedGraphActions : DirectedGraphDeclarableActions {

    fun createDirected(graphName: String)

}

interface UndirectedGraphDeclarableActions {

    fun addVertexToUndirectedGraph(graphName: String, vertex: Any)

    fun addEdgeToUndirectedGraph(graphName: String, source: Any, target: Any)

}

interface DirectedGraphDeclarableActions {

    fun addVertexToDirectedGraph(graphName: String, vertex: Any)

    fun addEdgeToDirectedGraph(graphName: String, source: Any, target: Any)

}
