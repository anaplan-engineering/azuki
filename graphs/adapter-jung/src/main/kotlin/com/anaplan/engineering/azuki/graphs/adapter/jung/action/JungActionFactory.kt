package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class JungActionFactory : GraphActionFactory {

    override fun create(graphName: String) = CreateGraphAction(graphName)
}

interface JungAction : Action {

    fun act(env: ExecutionEnvironment)

}


val toJungAction: (Action) -> JungAction = {
    @Suppress("UNCHECKED_CAST")
    it as? JungAction ?: throw IllegalArgumentException("Invalid action: $it")
}
