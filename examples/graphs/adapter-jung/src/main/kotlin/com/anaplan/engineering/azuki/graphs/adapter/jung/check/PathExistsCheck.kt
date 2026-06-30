package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.graphs.adapter.api.PathExistsBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath

class PathExistsCheck<V>(
    private val graphName: String,
    private val from: V,
    private val to: V,
    private val result: Boolean,
) : JungCheck, PathExistsBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(result, env.get(graphName) {
            val path = DijkstraShortestPath(this)
            val edges = path.getPath(from, to)
            edges.isNotEmpty()
        })

}
