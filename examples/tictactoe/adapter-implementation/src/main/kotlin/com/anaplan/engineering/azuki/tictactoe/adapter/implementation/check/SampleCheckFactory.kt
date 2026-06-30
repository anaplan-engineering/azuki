package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class SampleCheckFactory : TicTacToeCheckFactory {

    override val player = SamplePlayerCheckFactory
    override val game = SampleGameCheckFactory

    override fun systemValid() = SystemValidCheck()
}

object SamplePlayerCheckFactory : PlayerCheckFactory {

    override fun moveCount(gameName: String, playerName: String, times: Int) =
        PlayerMoveCountCheck(gameName, playerName, times)
    override fun canPlaceToken(gameName: String, playerName: String, position: Position, expected: Boolean) = CanPlaceTokenCheck(gameName, playerName, position, expected)
    override fun hasWon(gameName: String, playerName: String) = HasWonCheck(gameName, playerName)
    override fun hasLost(gameName: String, playerName: String) = HasLostCheck(gameName, playerName)
}

object SampleGameCheckFactory : GameCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = HasPlayOrderCheck(gameName, players)
    override fun hasToken(gameName: String, playerName: String, position: Position) = HasTokenCheck(gameName, playerName, position)
    override fun hasSpace(gameName: String, position: Position) = HasSpaceCheck(gameName, position)
    override fun hasState(gameName: String, moves: MoveMap) = HasStateCheck(gameName, moves)
    override fun isComplete(gameName: String) = IsCompleteCheck(gameName)
    override fun isDraw(gameName: String) = IsDrawnCheck(gameName)
}

interface SampleCheck : Check {

    fun check(env: ExecutionEnvironment): Boolean
}
