package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class SampleCheckFactory : TicTacToeCheckFactory {
    override val player = SamplePlayerCheckFactory
    override val game = SampleGameCheckFactory
}

object SamplePlayerCheckFactory : PlayerCheckFactory {
    override fun moveCount(gameName: String, playerName: String, times: Int) =
        PlayerMoveCountCheck(gameName, playerName, times)
    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) = UnsupportedCheck
    override fun hasWon(gameName: String, playerName: String) = UnsupportedCheck
    override fun hasLost(gameName: String, playerName: String) = UnsupportedCheck
}

object SampleGameCheckFactory : GameCheckFactory {
    override fun hasPlayOrder(gameName: String, playOrder: List<String>) = UnsupportedCheck
    override fun hasState(gameName: String, moves: MoveMap) = UnsupportedCheck
    override fun isComplete(gameName: String) = UnsupportedCheck
    override fun isDraw(gameName: String) = UnsupportedCheck
}

object UnsupportedCheck : Check { override val behavior = -1 }

interface SampleCheck : Check {

    fun check(env: ExecutionEnvironment): Boolean
}
