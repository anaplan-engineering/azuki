package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object GraphBehaviours {
    const val CreateGraph = 1
    const val GetVertexCount = 2
    const val AddVertex = 3
    const val AddEdge = 4
}

open class CreateGraphBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.CreateGraph
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
