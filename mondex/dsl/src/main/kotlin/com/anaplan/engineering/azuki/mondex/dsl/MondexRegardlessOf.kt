package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.RegardlessOf
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory

class MondexRegardlessOf(private val actionFactory: MondexActionFactory<*>) :
    RegardlessOf<MondexActionFactory<*>> {

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList
}
