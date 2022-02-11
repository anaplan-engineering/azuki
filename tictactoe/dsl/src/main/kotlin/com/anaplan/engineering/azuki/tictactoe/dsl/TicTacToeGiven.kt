package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.dsl.ascii.TicTacToeBoardAscii

class TicTacToeGiven(private val actionFactory: TicTacToeActionFactory): Given<TicTacToeActionFactory> {

    private val actionList = mutableListOf<Action>()

    fun thereIsAPlayOrder(orderName: String, vararg players: String) {
        actionList.add(actionFactory.playOrder.create(orderName, players.toList()))
    }

    fun thereIsANewGame(gameName: String, orderName: String) {
        actionList.add(actionFactory.game.start(gameName, orderName))
    }

    fun thereIsANewGameWithPlayers(gameName: String, vararg players: String) {
        val orderName = "${gameName}_ORDER"
        thereIsAPlayOrder(orderName, *players)
        thereIsANewGame(gameName, orderName)
    }

    fun thereIsANewGame(gameName: String) {
        thereIsANewGameWithPlayers(gameName, "X", "O")
    }

    fun thereIsAGame(gameName: String, orderName: String, boardData: String) {
        thereIsANewGame(gameName, orderName)
        TicTacToeBoardAscii.parse(boardData).forEach { (position, playerName) ->
            actionList.add(actionFactory.game.move(gameName, playerName, position))
        }
    }

    fun thereIsAGame(gameName: String, boardData: String) {
        thereIsANewGame(gameName)
        TicTacToeBoardAscii.parse(boardData).forEach { (position, playerName) ->
            actionList.add(actionFactory.game.move(gameName, playerName, position))
        }
    }

    override fun actions(): List<Action> = actionList

}
