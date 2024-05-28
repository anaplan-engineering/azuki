package com.anaplan.engineering.azuki.graphs.dsl.check

interface GraphChecks {

    fun hasVertexCount(graphName: String, count: Long)
    fun hasShortestPath(graphName: String, from: Any, vararg path: Any, to: Any)
    fun hasCycles(graphName: String, hasCycle: Boolean)
    fun getSimpleCycleCount(graphName: String, count: Long)
}
