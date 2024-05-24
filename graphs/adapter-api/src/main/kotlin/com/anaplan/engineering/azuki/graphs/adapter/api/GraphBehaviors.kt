package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object GraphBehaviors {
    const val CreateUndirectedGraph = 1
    const val GetVertexCount = 2
    const val AddVertex = 3
    const val AddEdge = 4
    const val GetShortestPath = 5
    const val HasCycles = 6
    const val GetSimpleCycleCount = 7
    const val CreateDirectedGraph = 8
}

open class CreateUndirectedGraphBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.CreateUndirectedGraph
}

open class GetVertexCountBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.GetVertexCount
}

open class AddVertexBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.AddVertex
}

open class AddEdgeBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.AddEdge
}

open class GetShortestPathBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.GetShortestPath
}

open class HasCyclesBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.HasCycles
}

open class GetCycleCountBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.GetSimpleCycleCount
}

open class CreateDirectedGraphBehavior : ReifiedBehavior {
    override val behavior = GraphBehaviors.CreateDirectedGraph
}
