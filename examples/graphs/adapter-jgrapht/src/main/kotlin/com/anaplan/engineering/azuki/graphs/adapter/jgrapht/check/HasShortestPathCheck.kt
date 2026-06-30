package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val path: List<V>,
) : JGraphTCheck, GetShortestPathBehaviour() {

    private val from = path.first()
    private val to = path.last()

    override fun check(env: ExecutionEnvironment) =
        checkEqual(path, env.get<V, List<V>>(graphName) {
            val pathAlg = DijkstraShortestPath(this as Graph<V, DefaultEdge>)
            pathAlg.getPath(from, to).vertexList
        })

}
