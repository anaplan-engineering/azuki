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
            throw LateDetectUnsupportedCheckException("path must contain at least its two endpoints")
        }
    }

    private val from by lazy { path.first() }
    private val to by lazy { path.last() }

    override fun check(env: ExecutionEnvironment) =
        checkEqual(path, env.get(graphName) {
            val pathAlg = DijkstraShortestPath(this)
            val edges = pathAlg.getPath(from, to)
            val edgeMap = pathAlg.getIncomingEdgeMap(from)
            listOf(from) + edgeMap.filter { it.value in edges }.keys.toList()
        })
}
