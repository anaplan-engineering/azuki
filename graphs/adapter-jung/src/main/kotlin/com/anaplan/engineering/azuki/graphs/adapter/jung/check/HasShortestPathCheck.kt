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
        checkEqual(fullShortestPath, env.get<V, List<V>>(graphName) {
            val pathAlg = DijkstraShortestPath(this)
            val edges = pathAlg.getPath(from, to)
            edges.first().toList() + edges.drop(1).map { it.second }
        })

}
