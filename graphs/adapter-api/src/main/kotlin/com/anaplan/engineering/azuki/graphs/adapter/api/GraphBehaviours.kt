package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object GraphBehaviours {
    const val CreateUndirectedGraph = 1
    const val GetVertexCount = 2
    const val AddVertex = 3
    const val AddEdge = 4
    const val GetShortestPath = 5
    const val HasCycles = 6
    const val GetSimpleCycleCount = 7
    const val CreateDirectedGraph = 8
    const val PathExists = 9
}

open class CreateUndirectedGraphBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.CreateUndirectedGraph
}

open class GetVertexCountBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.GetVertexCount
}

open class AddVertexBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.AddVertex
}

open class AddEdgeBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.AddEdge
}

open class GetShortestPathBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.GetShortestPath
}

open class HasCyclesBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.HasCycles
}

open class GetCycleCountBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.GetSimpleCycleCount
}

open class CreateDirectedGraphBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.CreateDirectedGraph
}

open class PathExistsBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.PathExists
}
