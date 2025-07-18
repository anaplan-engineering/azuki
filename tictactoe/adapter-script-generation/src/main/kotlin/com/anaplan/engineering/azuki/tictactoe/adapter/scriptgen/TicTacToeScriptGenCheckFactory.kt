package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.ComposableCheck
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeThen

object TicTacToeScriptGenCheckFactory : TicTacToeCheckFactory {

    override val game = GameScriptGenCheckFactory
    override val player = PlayerScriptGenCheckFactory
}

abstract class TicTacToeComposableCheck : ComposableCheck<TicTacToeCheckState> {

    override val behavior = unsupportedBehavior
}

abstract class ScriptGenCheck : ScriptGenerationCheck {

    override val behavior = unsupportedBehavior
}

object GameScriptGenCheckFactory : GameCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = object : ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameHasPlayOrder, gameName, players)
    }

    override fun hasToken(gameName: String, playerName: String, position: Position) = object : TicTacToeComposableCheck() {

        override fun composeInto(state: TicTacToeCheckState) {
            state.addToken(gameName, playerName, position)
        }
    }

    override fun hasSpace(gameName: String, position: Position) = object : TicTacToeComposableCheck() {

        override fun composeInto(state: TicTacToeCheckState) {
            state.addSpace(gameName, position)
        }
    }

    override fun hasState(gameName: String, moves: MoveMap) = object : ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasState, gameName, moves.pretty(3, 3))
    }

    override fun isComplete(gameName: String) = object : ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardIsComplete, gameName)
    }

    override fun isDraw(gameName: String) = object : ScriptGenCheck() {

        override fun getCheckScript() = TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameIsDraw, gameName)
    }
}

object PlayerScriptGenCheckFactory : PlayerCheckFactory {

    override fun moveCount(gameName: String, playerName: String, times: Int) = object : ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasMoved, gameName, playerName, times)
    }

    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) =
        object : ScriptGenCheck() {

            override fun getCheckScript() =
                TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerCannotPlaceToken,
                    gameName,
                    playerName,
                    position)
        }

    override fun hasWon(gameName: String, playerName: String) = object : ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasWon, gameName, playerName)
    }

    override fun hasLost(gameName: String, playerName: String) = object : ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasLost, gameName, playerName)
    }
}
