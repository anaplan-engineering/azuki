package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val shortestPath: List<V>,
) : JungCheck, GetShortestPathBehaviour() {

    private val from = try {
        shortestPath.first()
    } catch (e: NoSuchElementException) {
        error("hasShortestPath must has at least three arguments: graphName, startVertex, endVertex")
    }
    private val to = shortestPath.last()

    override fun check(env: ExecutionEnvironment) =
        checkEqual(shortestPath, env.get(graphName) {
            if (shortestPath.size < 2) {
                error("hasShortestPath must has at least three arguments: graphName, startVertex, endVertex")
            }
            val pathAlg = DijkstraShortestPath(this)
            val edges = pathAlg.getPath(from, to)
            val edgeMap = pathAlg.getIncomingEdgeMap(from)
            val vertexList = edgeMap.filter { it.value in edges }.keys.toList()
            if (vertexList == emptyList<V>()) {
                vertexList
            } else {
                listOf(from) + vertexList
            }
        })
}
