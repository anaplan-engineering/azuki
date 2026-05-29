package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

interface RightOfWayCheckFactory : CheckFactory {

    val player: PlayerCheckFactory
    val game: GameCheckFactory
}

interface PlayerCheckFactory {

    fun moveCount(gameName: String, playerName: String, times: Int): Check
    fun canPlaceToken(gameName: String, playerName: String, position: Position, expected: Boolean): Check
    fun hasWon(gameName: String, playerName: String): Check
    fun hasLost(gameName: String, playerName: String): Check
}

interface GameCheckFactory {

    fun hasPlayOrder(gameName: String, players: List<String>): Check
    fun hasToken(gameName: String, playerName: String, position: Position): Check
    fun hasSpace(gameName: String, position: Position): Check
    fun hasState(gameName: String, moves: MoveMap): Check
    fun isComplete(gameName: String): Check
    fun isDraw(gameName: String): Check
}
