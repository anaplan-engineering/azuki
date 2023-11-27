package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.EnvironmentBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment

class KazukiCheckFactory : TicTacToeCheckFactory {
    override val player = KazukiPlayerCheckFactory
    override val game = KazukiGameCheckFactory
}

object KazukiPlayerCheckFactory : PlayerCheckFactory {
    override fun moveCount(gameName: String, playerName: String, times: Int) =
        PlayerMoveCountCheck(gameName, playerName, times)

    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) =
        CannotPlaceTokenCheck(gameName, playerName, position)

    override fun hasWon(gameName: String, playerName: String) = UnsupportedCheck
    override fun hasLost(gameName: String, playerName: String) = UnsupportedCheck
}

object KazukiGameCheckFactory : GameCheckFactory {
    override fun hasPlayOrder(gameName: String, players: List<String>) = HasPlayOrderCheck(gameName, players)
    override fun hasState(gameName: String, moves: MoveMap) = BoardHasStateCheck(gameName, moves)
    override fun isComplete(gameName: String) = BoardIsCompleteCheck(gameName)
    override fun isDraw(gameName: String) = UnsupportedCheck
}

interface KazukiCheck : Check {

    fun check(env: ExecutionEnvironment): Boolean
}
