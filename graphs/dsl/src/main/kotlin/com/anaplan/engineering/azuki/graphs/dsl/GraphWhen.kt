package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory

class GraphWhen(val actionFactory: GraphActionFactory): When<GraphActionFactory> {

    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

}
