package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory

class RightOfWayThen(private val checkFactory: RightOfWayCheckFactory): Then<RightOfWayCheckFactory> {

    private val checkList = mutableListOf<Check>()

    override fun checks() = checkList

    fun everythingIsOkay() {
        checkList.add(checkFactory.systemValid())
    }

    fun playerHasMoved(gameName: String, playerName: String, times: Int) {
        checkList.add(checkFactory.player.moveCount(gameName, playerName, times))
    }

    fun playerCanPlaceToken(gameName: String, playerName: String, position: Pair<Int, Int>) {
        checkList.add(checkFactory.player.canPlaceToken(gameName, playerName, Position(position), expected = true))
    }

    fun playerCannotPlaceToken(gameName: String, playerName: String, position: Pair<Int, Int>) {
        checkList.add(checkFactory.player.canPlaceToken(gameName, playerName, Position(position), expected = false))
    }

    fun gameHasPlayOrder(gameName: String, vararg players: String) {
        checkList.add(checkFactory.game.hasPlayOrder(gameName, players.toList()))
    }

////    fun boardHasState(gameName: String, boardData: String) {
////        checkList.add(checkFactory.game.hasState(gameName, RightOfWayBoardAscii.parse(boardData)))
//    }

    fun boardHasToken(gameName: String, playerName: String, position: Pair<Int, Int>) {
        checkList.add(checkFactory.game.hasToken(gameName, playerName, Position(position)))
    }

    fun boardHasSpace(gameName: String, position: Pair<Int, Int>) {
        checkList.add(checkFactory.game.hasSpace(gameName, Position(position)))
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
}
