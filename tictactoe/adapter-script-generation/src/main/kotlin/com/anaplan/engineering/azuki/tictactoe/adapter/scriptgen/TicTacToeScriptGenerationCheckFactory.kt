package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.ComposableScriptGenerationCheck
import com.anaplan.engineering.azuki.script.generation.BasicScriptGenerationCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeThen

object TicTacToeScriptGenerationCheckFactory : TicTacToeCheckFactory {

    override val game = GameScriptGenerationCheckFactory
    override val player = PlayerScriptGenCheckFactory
}

fun interface TicTacToeComposableScriptGenerationCheck :
    ComposableScriptGenerationCheck<TicTacToeGenerationEnvironment> {

    override val behavior get() = unsupportedBehavior
}

fun interface TicTacToeBasicScriptGenerationCheck : BasicScriptGenerationCheck {

    override val behavior get() = unsupportedBehavior
}

object GameScriptGenerationCheckFactory : GameCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = TicTacToeBasicScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameHasPlayOrder, gameName, players)
    }

    override fun hasToken(gameName: String, playerName: String, position: Position) =
        TicTacToeComposableScriptGenerationCheck {
            it.composeBoardCheck(gameName) { addToken(playerName, position) }
        }

    override fun hasSpace(gameName: String, position: Position) = TicTacToeComposableScriptGenerationCheck {
        it.composeBoardCheck(gameName) { addSpace(position) }
    }

    override fun hasState(gameName: String, moves: MoveMap) = TicTacToeBasicScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasState, gameName, moves.pretty(3, 3))
    }

    override fun isComplete(gameName: String) = TicTacToeBasicScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardIsComplete, gameName)
    }

    override fun isDraw(gameName: String) = TicTacToeBasicScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameIsDraw, gameName)
    }
}

object PlayerScriptGenCheckFactory : PlayerCheckFactory {

    override fun moveCount(gameName: String, playerName: String, times: Int) = TicTacToeBasicScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasMoved, gameName, playerName, times)
    }

    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) =
        TicTacToeBasicScriptGenerationCheck {
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerCannotPlaceToken,
                gameName,
                playerName,
                position)
        }

    override fun hasWon(gameName: String, playerName: String) = TicTacToeBasicScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasWon, gameName, playerName)
    }

    override fun hasLost(gameName: String, playerName: String) = TicTacToeBasicScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasLost, gameName, playerName)
    }
}
