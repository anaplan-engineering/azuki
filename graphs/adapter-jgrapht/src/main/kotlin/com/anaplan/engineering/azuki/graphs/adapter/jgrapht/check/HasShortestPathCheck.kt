package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val from: V,
    private val to: V,
    private val shortestPath: List<V>
) : JGraphTCheck, GetShortestPathBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(shortestPath, env.get<V, List<V>>(graphName) {
            val pathAlg = DijkstraShortestPath(this as Graph<V, DefaultEdge>)
            val path = pathAlg.getPath(from, to)
            if (path == null) {
                emptyList()
            } else {
                path.vertexList
            }
        })

}
