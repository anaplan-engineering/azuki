package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl

import com.anaplan.engineering.azuki.core.dsl.RegardlessOf
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory

class IntraWorldRegardlessOf(private val actionFactory: IntraWorldActionFactory<*>) :
    RegardlessOf<IntraWorldActionFactory<*>> {

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList
}
