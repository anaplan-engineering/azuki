package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory

class MondexWhen(private val actionFactory: MondexActionFactory<*>) :
    When<MondexActionFactory<*>> {

    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList
}
