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

abstract class TicTacToeComposableScriptGenerationCheck : ComposableScriptGenerationCheck<TicTacToeGenerationEnvironment> {

    override val behavior = unsupportedBehavior
}

abstract class TicTacToeBasicScriptGenerationCheck : BasicScriptGenerationCheck {

    override val behavior = unsupportedBehavior
}

object GameScriptGenerationCheckFactory : GameCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = object : TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameHasPlayOrder, gameName, players)
    }

    override fun hasToken(gameName: String, playerName: String, position: Position) = object : TicTacToeComposableScriptGenerationCheck() {

        override fun composeInto(environment: TicTacToeGenerationEnvironment) {
            environment.composeBoardCheck(gameName) { addToken(playerName, position) }
        }
    }

    override fun hasSpace(gameName: String, position: Position) = object : TicTacToeComposableScriptGenerationCheck() {

        override fun composeInto(environment: TicTacToeGenerationEnvironment) {
            environment.composeBoardCheck(gameName) { addSpace(position) }
        }
    }

    override fun hasState(gameName: String, moves: MoveMap) = object : TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasState, gameName, moves.pretty(3, 3))
    }

    override fun isComplete(gameName: String) = object : TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardIsComplete, gameName)
    }

    override fun isDraw(gameName: String) = object : TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() = TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::gameIsDraw, gameName)
    }
}

object PlayerScriptGenCheckFactory : PlayerCheckFactory {

    override fun moveCount(gameName: String, playerName: String, times: Int) = object : TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasMoved, gameName, playerName, times)
    }

    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) =
        object : TicTacToeBasicScriptGenerationCheck() {

            override fun getCheckScript() =
                TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerCannotPlaceToken,
                    gameName,
                    playerName,
                    position)
        }

    override fun hasWon(gameName: String, playerName: String) = object : TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasWon, gameName, playerName)
    }

    override fun hasLost(gameName: String, playerName: String) = object : TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::playerHasLost, gameName, playerName)
    }
}
