package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.dsl.check.GraphChecks

class GraphThen(private val checkFactory: GraphCheckFactory) : Then<GraphCheckFactory>,
    GraphChecks {

    private val checkList = mutableListOf<Check>()

    override fun checks() = checkList

    override fun hasVertexCount(graphName: String, count: Long) {
        checkList.add(checkFactory.hasVertexCount(graphName, count))
    }

    override fun hasShortestPath(graphName: String, from: Any, to: Any, vararg path: Any) {
        checkList.add(checkFactory.hasShortestPath(graphName, from, to, path.toList()))
    }

    override fun hasCycles(graphName: String, hasCycles: Boolean) {
        checkList.add(checkFactory.hasCycles(graphName, hasCycles))
    }

    override fun hasSimpleCycleCount(graphName: String, count: Long) {
        checkList.add(checkFactory.hasSimpleCycleCount(graphName, count))
    }

    override fun pathExists(graphName: String, from: Any, to: Any) {
        checkList.add(checkFactory.pathExists(graphName, from, to, true))
    }

    override fun noPathExists(graphName: String, from: Any, to: Any) {
        checkList.add(checkFactory.pathExists(graphName, from, to, false))
    }

}
