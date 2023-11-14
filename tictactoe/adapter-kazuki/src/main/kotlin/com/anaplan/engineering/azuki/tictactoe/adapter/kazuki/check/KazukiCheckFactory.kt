package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.AnimationBuilder

class KazukiCheckFactory : TicTacToeCheckFactory {
    override val player = KazukiPlayerCheckFactory
    override val game = KazukiGameCheckFactory
}

object KazukiPlayerCheckFactory : PlayerCheckFactory {
    override fun moveCount(gameName: String, playerName: String, times: Int) = UnsupportedCheck
    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) = UnsupportedCheck
    override fun hasWon(gameName: String, playerName: String) = UnsupportedCheck
    override fun hasLost(gameName: String, playerName: String) = UnsupportedCheck
}

object KazukiGameCheckFactory : GameCheckFactory {
    override fun hasPlayOrder(gameName: String, players: List<String>) = UnsupportedCheck
    override fun hasState(gameName: String, moves: MoveMap) = UnsupportedCheck
    override fun isComplete(gameName: String) = UnsupportedCheck
    override fun isDraw(gameName: String) = UnsupportedCheck
}

interface KazukiCheck : Check {

    fun check(env: AnimationBuilder): Boolean
}
