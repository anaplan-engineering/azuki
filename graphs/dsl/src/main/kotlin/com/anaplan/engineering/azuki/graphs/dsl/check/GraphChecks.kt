package com.anaplan.engineering.azuki.graphs.dsl.check

interface GraphChecks {

    fun hasVertexCount(graphName: String, count: Long)
    fun hasShortestPath(graphName: String, from: Any, to: Any, vararg path: Any)
}
