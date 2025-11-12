package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO

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

    override fun canPlaceToken(gameName: String, playerName: String, position: Position) =
        CanPlaceTokenCheck(gameName, playerName, position)

    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) =
        CannotPlaceTokenCheck(gameName, playerName, position)

    override fun hasWon(gameName: String, playerName: String): Check = object : HasWonBehaviour(), KazukiCheck {

        override fun check(env: ExecutionEnvironment) = XO.hasWon(env.game(gameName), playerName.toPlayer())
    }

    override fun hasLost(gameName: String, playerName: String): Check = object : HasWonBehaviour(), KazukiCheck {

        override fun check(env: ExecutionEnvironment) = XO.hasLost(env.game(gameName), playerName.toPlayer())
    }
}

object KazukiGameCheckFactory : GameCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = HasPlayOrderCheck(gameName, players)

    override fun hasToken(gameName: String, playerName: String, position: Position): Check =
        object : PlaceATokenBehaviour(), KazukiCheck {

            override fun check(env: ExecutionEnvironment) =
                env.game(gameName).board[position.toKazuki()] == playerName.toPlayer()
        }

    override fun hasSpace(gameName: String, position: Position): Check = object : PlaceATokenBehaviour(), KazukiCheck {

        override fun check(env: ExecutionEnvironment) = position.toKazuki() !in env.game(gameName).board.dom
    }

    override fun hasState(gameName: String, moves: MoveMap) = BoardHasStateCheck(gameName, moves)
    override fun isComplete(gameName: String) = BoardIsCompleteCheck(gameName)
    override fun isDraw(gameName: String): Check = object : IsDrawnBehaviour(), KazukiCheck {

        override fun check(env: ExecutionEnvironment) = XO.isDraw(env.game(gameName))
    }
}

interface KazukiCheck : Check {

    fun check(env: ExecutionEnvironment): Boolean
}
