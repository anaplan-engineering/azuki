package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.RegardlessOf
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory

class GraphRegardlessOf(private val actionFactory: GraphActionFactory): RegardlessOf<GraphActionFactory> {

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList

}
