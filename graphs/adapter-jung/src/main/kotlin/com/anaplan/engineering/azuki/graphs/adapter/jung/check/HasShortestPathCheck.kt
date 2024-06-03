package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedCheckException
import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val path: List<V>,
) : JungCheck, GetShortestPathBehaviour() {

    init {
        if (path.size < 2) {
            throw LateDetectUnsupportedCheckException("hasShortestPath must take a start and end vertex in the path, as inputs")
        }
    }

    private val from = path.first()
    private val to = path.last()

    override fun check(env: ExecutionEnvironment) =
        checkEqual(path, env.get(graphName) {
            val pathAlg = DijkstraShortestPath(this)
            val edges = pathAlg.getPath(from, to)
            val edgeMap = pathAlg.getIncomingEdgeMap(from)
            edgeMap.filter { it.value in edges }.keys.toList()
        })
}
