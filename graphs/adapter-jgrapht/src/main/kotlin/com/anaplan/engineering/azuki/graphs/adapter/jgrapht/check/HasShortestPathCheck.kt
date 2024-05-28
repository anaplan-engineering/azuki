package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val shortestPath: List<V>,
) : JGraphTCheck, GetShortestPathBehaviour() {

    private val from = try {
        shortestPath.first()
    } catch (e: NoSuchElementException) {
        error("hasShortestPath must has at least three arguments: graphName, startVertex, endVertex")
    }
    private val to = shortestPath.last()

    override fun check(env: ExecutionEnvironment) =
        checkEqual(shortestPath, env.get<V, List<V>>(graphName) {
            if (shortestPath.size < 2) {
                error("hasShortestPath must has at least three arguments: graphName, startVertex, endVertex")
            }
            val pathAlg = DijkstraShortestPath(this as Graph<V, DefaultEdge>)
            val path = pathAlg.getPath(from, to)
            if (path == null) {
                emptyList()
            } else {
                path.vertexList
            }
        })

}
