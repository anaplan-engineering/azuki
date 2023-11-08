package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.RegardlessOf
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory

class TicTacToeRegardlessOf(private val actionFactory: TicTacToeActionFactory) : RegardlessOf<TicTacToeActionFactory> {

    private val actionList = mutableListOf<Action>()

    fun saveGame(gameName: String) {
        actionList.add(actionFactory.game.save(gameName))
    }

    fun closeGame(gameName: String) {
        actionList.add(actionFactory.game.close(gameName))
    }

    fun loadGame(gameName: String) {
        actionList.add(actionFactory.game.load(gameName))
    }

    override fun actions(): List<Action> = actionList

}
