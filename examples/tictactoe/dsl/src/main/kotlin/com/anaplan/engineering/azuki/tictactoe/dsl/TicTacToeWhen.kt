package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory

class TicTacToeWhen(val actionFactory: TicTacToeActionFactory): When<TicTacToeActionFactory> {

    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

    fun placeToken(gameName: String, playerName: String, position: Pair<Int, Int>) {
        actionList.add(actionFactory.game.move(gameName, playerName, Position(position)))
    }
}
