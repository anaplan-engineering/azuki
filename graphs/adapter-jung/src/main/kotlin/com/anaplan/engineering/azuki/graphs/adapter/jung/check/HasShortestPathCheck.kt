package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedCheckException
import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val shortestPath: List<V>,
) : JungCheck, GetShortestPathBehaviour() {

    init {
        if (shortestPath.size < 2) {
            throw LateDetectUnsupportedCheckException("")
        }
    }

    private val from = shortestPath.first()
    private val to = shortestPath.last()

    override fun check(env: ExecutionEnvironment) =
        checkEqual(shortestPath, env.get(graphName) {
            val pathAlg = DijkstraShortestPath(this)
            val edges = pathAlg.getPath(from, to)
            val edgeMap = pathAlg.getIncomingEdgeMap(from)
            edgeMap.filter { it.value in edges }.keys.toList()
        })
}
