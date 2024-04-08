package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val from: V,
    private val to: V,
    private val shortestPath: List<V>
) : JungCheck, GetShortestPathBehaviour() {

    private val fullShortestPath by lazy {
        listOf(from) + shortestPath + to
    }

    override fun check(env: ExecutionEnvironment) =
        checkEqual(fullShortestPath, env.get(graphName) {
            val shortestPath = DijkstraShortestPath(this)
            val edges = shortestPath.getPath(from, to)
            val edgeMap = shortestPath.getIncomingEdgeMap(from)
            listOf(from) + edgeMap.filter { it.value in edges }.keys.toList()
        })
}
