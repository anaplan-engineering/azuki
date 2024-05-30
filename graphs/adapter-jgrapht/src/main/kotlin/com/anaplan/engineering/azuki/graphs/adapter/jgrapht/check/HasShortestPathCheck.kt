package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedCheckException
import com.anaplan.engineering.azuki.graphs.adapter.api.GetShortestPathBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge


class HasShortestPathCheck<V>(
    private val graphName: String,
    private val shortestPath: List<V>,
) : JGraphTCheck, GetShortestPathBehaviour() {

    init {
        if (shortestPath.size < 2) {
            throw LateDetectUnsupportedCheckException("")
        }
    }

    private val from = shortestPath.first()
    private val to = shortestPath.last()

    override fun check(env: ExecutionEnvironment) =
        checkEqual(shortestPath, env.get<V, List<V>>(graphName) {
            val pathAlg = DijkstraShortestPath(this as Graph<V, DefaultEdge>)
            pathAlg.getPath(from, to).vertexList
        })

}
