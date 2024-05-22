package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.graphs.adapter.api.HasPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath

class HasPathCheck(
    private val graphName: String,
    private val from: Any,
    private val to: Any,
    private val result: Boolean,
) : JungCheck, HasPathBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(result, env.get(graphName) {
            val path = DijkstraShortestPath(this)
            val edges = path.getPath(from, to)
            edges.isNotEmpty()
        })

}
