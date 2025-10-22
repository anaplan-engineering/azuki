package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationCheck
import com.anaplan.engineering.azuki.script.generation.asComposable
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeThen

object TicTacToeScriptGenerationCheckFactory : TicTacToeCheckFactory {

    override val game = GameScriptGenerationCheckFactory
    override val player = PlayerScriptGenCheckFactory
}

fun interface TicTacToeScriptGenerationCheck : ScriptGenerationCheck<TicTacToeGenerationEnvironment> {

    override val behavior get() = unsupportedBehavior
}

object GameScriptGenerationCheckFactory : GameCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameHasPlayOrder, gameName, players)
    }

    override fun hasToken(gameName: String, playerName: String, position: Position) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasToken, gameName, playerName, position)
    }.asComposable {
        boardCheckStates.register(gameName) { addToken(playerName, position) }
    }

    override fun hasSpace(gameName: String, position: Position) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasSpace, gameName, position)
    }.asComposable {
        boardCheckStates.register(gameName) { addSpace(position) }
    }

    override fun hasState(gameName: String, moves: MoveMap) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasState, gameName, moves.pretty(3, 3))
    }

    override fun isComplete(gameName: String) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardIsComplete, gameName)
    }

    override fun isDraw(gameName: String) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameIsDraw, gameName)
    }
}

object PlayerScriptGenCheckFactory : PlayerCheckFactory {

    override fun moveCount(gameName: String, playerName: String, times: Int) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasMoved, gameName, playerName, times)
    }

    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) =
        TicTacToeScriptGenerationCheck {
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerCannotPlaceToken,
                gameName,
                playerName,
                position)
        }

    override fun hasWon(gameName: String, playerName: String) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasWon, gameName, playerName)
    }

    override fun hasLost(gameName: String, playerName: String) = TicTacToeScriptGenerationCheck {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasLost, gameName, playerName)
    }
}
