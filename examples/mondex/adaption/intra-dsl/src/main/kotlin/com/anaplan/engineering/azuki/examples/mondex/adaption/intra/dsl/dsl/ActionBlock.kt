package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory

abstract class ActionBlock(protected val actionFactory: IntraWorldActionFactory<*>) {
    private val actionList = mutableListOf<Action>()

    protected fun add(action: Action) = actionList.add(action)

    protected fun add(actions: List<Action>) = actionList.addAll(actions)

    fun actions(): List<Action> = actionList
}
