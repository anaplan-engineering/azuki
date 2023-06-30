package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

enum class GraphBehaviours {
    CreateGraph
}

open class CreateGraphBehaviour : ReifiedBehavior {
    override val behavior = GraphBehaviours.CreateGraph.ordinal
}
