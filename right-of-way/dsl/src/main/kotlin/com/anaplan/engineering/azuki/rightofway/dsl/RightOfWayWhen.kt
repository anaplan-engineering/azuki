package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory

class RightOfWayWhen(val actionFactory: RightOfWayActionFactory): When<RightOfWayActionFactory> {

    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

//    fun placeToken(gameName: String, playerName: String, position: Pair<Int, Int>) {
//        actionList.add(actionFactory.game.move(gameName, playerName, Position(position)))
//    }
}
