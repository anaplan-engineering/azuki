package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory

@ScenarioDsl
class PurseBlock(
    private val balance: ULong,
    private val lost: ULong,
    private val actionFactory: MondexActionFactory<*>
) {
    private val actionList = mutableListOf<Action>()

    fun actions(): List<Action> = actionList
}
