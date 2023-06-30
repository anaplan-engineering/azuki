package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory

class JGraphTActionFactory: GraphActionFactory {

    // TODO - directed etc
    override fun create(graphName: String) = CreateGraphAction(graphName)

}


interface JGraphTAction: Action


val toJGraphTAction: (Action) -> JGraphTAction = {
    @Suppress("UNCHECKED_CAST")
    it as? JGraphTAction ?: throw IllegalArgumentException("Invalid action: $it")
}
