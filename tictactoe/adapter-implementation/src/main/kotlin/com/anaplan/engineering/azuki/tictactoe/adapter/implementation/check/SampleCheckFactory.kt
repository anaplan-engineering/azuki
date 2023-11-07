package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class SampleCheckFactory : TicTacToeCheckFactory {
    override val player = SamplePlayerCheckFactory
    override val game = SampleGameCheckFactory
}

object SamplePlayerCheckFactory : PlayerCheckFactory {
    override fun moveCount(gameName: String, playerName: String, times: Int) =
        PlayerMoveCountCheck(gameName, playerName, times)
    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) = CannotPlaceTokenCheck(gameName, playerName, position)
    override fun hasWon(gameName: String, playerName: String) = HasWonCheck(gameName, playerName)
    override fun hasLost(gameName: String, playerName: String) = HasLostCheck(gameName, playerName)
}

object SampleGameCheckFactory : GameCheckFactory {
    override fun hasPlayOrder(gameName: String, players: List<String>) = HasPlayOrderCheck(gameName, players)
    override fun hasState(gameName: String, moves: MoveMap) = HasStateCheck(gameName, moves)
    override fun isComplete(gameName: String) = IsCompleteCheck(gameName)
    override fun isDraw(gameName: String) = IsDrawnCheck(gameName)
}

interface SampleCheck : Check {

    fun check(env: ExecutionEnvironment): Boolean
}
