package com.anaplan.engineering.azuki.graphs.dsl.check

interface GraphChecks {

    fun hasVertexCount(graphName: String, count: Long)
    fun hasShortestPath(graphName: String, vararg path: Any)
    fun hasCycles(graphName: String, hasCycle: Boolean)
    fun hasSimpleCycleCount(graphName: String, count: Long)
    fun pathExists(graphName: String, from: Any, to: Any)
    fun noPathExists(graphName: String, from: Any, to: Any)
    fun hasEdgeCount(graphName: String, count: Long)
}
