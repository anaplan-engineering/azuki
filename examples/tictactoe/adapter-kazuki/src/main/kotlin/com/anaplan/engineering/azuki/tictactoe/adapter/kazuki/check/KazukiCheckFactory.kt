package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment

class KazukiCheckFactory : TicTacToeCheckFactory {

    override val player = KazukiPlayerCheckFactory
    override val game = KazukiGameCheckFactory

    override fun systemValid() = object : KazukiCheck {

        override val behavior = unsupportedBehavior
        override fun check(env: ExecutionEnvironment) = true
    }
}

object KazukiPlayerCheckFactory : PlayerCheckFactory {

    override fun moveCount(gameName: String, playerName: String, times: Int) =
        PlayerMoveCountCheck(gameName, playerName, times)

    override fun canPlaceToken(gameName: String, playerName: String, position: Position, expected: Boolean) =
        CanPlaceTokenCheck(gameName, playerName, position, expected)

    override fun hasWon(gameName: String, playerName: String): Check = HasWonCheck(gameName, playerName)

    override fun hasLost(gameName: String, playerName: String): Check = HasLostCheck(gameName, playerName)
}

object KazukiGameCheckFactory : GameCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = HasPlayOrderCheck(gameName, players)

    override fun hasToken(gameName: String, playerName: String, position: Position) =
        HasTokenCheck(gameName, position, playerName)

    override fun hasSpace(gameName: String, position: Position) = HasSpaceCheck(position, gameName)
    override fun hasState(gameName: String, moves: MoveMap) = BoardHasStateCheck(gameName, moves)
    override fun isComplete(gameName: String) = BoardIsCompleteCheck(gameName)
    override fun isDraw(gameName: String): Check = IsDrawCheck(gameName)
}

interface KazukiCheck : Check {

    fun check(env: ExecutionEnvironment): Boolean
}
