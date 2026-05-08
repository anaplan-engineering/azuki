package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory

class MondexGiven(private val actionFactory: MondexActionFactory<*>) : Given<MondexActionFactory<*>> {

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList
}
