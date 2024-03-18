package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.dsl.ascii.TicTacToeBoardAscii

class TicTacToeThen(private val checkFactory: TicTacToeCheckFactory): Then<TicTacToeCheckFactory> {

    private val checkList = mutableListOf<Check>()

    override fun checks() = checkList

    fun playerHasMoved(gameName: String, playerName: String, times: Int) {
        checkList.add(checkFactory.player.moveCount(gameName, playerName, times))
    }

    fun playerCannotPlaceToken(gameName: String, playerName: String, position: Pair<Int, Int>) {
        checkList.add(checkFactory.player.cannotPlaceToken(gameName, playerName, Position(position)))
    }

    fun gameHasPlayOrder(gameName: String, vararg players: String) {
        checkList.add(checkFactory.game.hasPlayOrder(gameName, players.toList()))
    }

    fun boardHasState(gameName: String, boardData: String) {
        checkList.add(checkFactory.game.hasState(gameName, TicTacToeBoardAscii.parse(boardData)))
    }

    fun boardIsComplete(gameName: String) {
        checkList.add(checkFactory.game.isComplete(gameName))
    }

    fun playerHasWon(gameName: String, playerName: String) {
        checkList.add(checkFactory.player.hasWon(gameName, playerName))
    }

    fun playerHasLost(gameName: String, playerName: String) {
        checkList.add(checkFactory.player.hasLost(gameName, playerName))
    }

    fun gameIsDraw(gameName: String) {
        checkList.add(checkFactory.game.isDraw(gameName))
    }

    fun everythingIsOkay() {
        checkList.add(checkFactory.sanityCheck())
    }
}
