package com.anaplan.engineering.azuki.graphs.dsl.action

interface GraphActions : GraphDeclarableActions {

    fun create(graphName: String)
}

interface GraphDeclarableActions {

    fun addVertex(graphName: String, vertex: Any)

    fun addEdge(graphName: String, source: Any, target: Any)

}
