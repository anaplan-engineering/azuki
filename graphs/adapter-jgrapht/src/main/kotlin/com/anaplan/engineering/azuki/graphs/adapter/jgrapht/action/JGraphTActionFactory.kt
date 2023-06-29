package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory

class JGraphTActionFactory: GraphActionFactory {

    override fun create(graphName: String): Action {
        TODO("Not yet implemented")
    }

}


interface JGraphTAction: Action


val toJGraphTAction: (Action) -> JGraphTAction = {
    @Suppress("UNCHECKED_CAST")
    it as? JGraphTAction ?: throw IllegalArgumentException("Invalid action: $it")
}
