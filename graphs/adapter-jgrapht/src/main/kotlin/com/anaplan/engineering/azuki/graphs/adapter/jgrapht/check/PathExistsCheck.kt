package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.graphs.adapter.api.PathExistsBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge

class PathExistsCheck<V>(
    private val graphName: String,
    private val from: V,
    private val to: V,
    private val result: Boolean,
) : JGraphTCheck, PathExistsBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(result, env.get<V, Boolean>(graphName) {
            val path = DijkstraShortestPath(this as Graph<V, DefaultEdge>)
            path.getPath(from, to) != null
        })

}
