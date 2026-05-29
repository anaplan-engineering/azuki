package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.RegardlessOf
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory

class RightOfWayRegardlessOf(private val actionFactory: RightOfWayActionFactory) : RegardlessOf<RightOfWayActionFactory> {

    private val actionList = mutableListOf<Action>()

//    fun saveGame(gameName: String) {
//        actionList.add(actionFactory.game.save(gameName))
//    }
//
//    fun closeGame(gameName: String) {
//        actionList.add(actionFactory.game.close(gameName))
//    }
//
//    fun loadGame(gameName: String) {
//        actionList.add(actionFactory.game.load(gameName))
//    }

    override fun actions(): List<Action> = actionList

}
