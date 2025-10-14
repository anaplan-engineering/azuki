package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.IgnoreEnvScriptGenerationAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.GameActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayOrderActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.CreatePlayOrderDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.PlayerMoveDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.StartAGameDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeRegardlessOf
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeWhen

object TicTacToeScriptGenerationActionFactory : TicTacToeActionFactory {

    override val game = GameScriptGenerationActionFactory
    override val playOrder = PlayOrderScriptGenActionFactory
}

abstract class TicTacToeScriptGenerationAction : IgnoreEnvScriptGenerationAction<TicTacToeGenerationEnvironment> {

    override val behavior = unsupportedBehavior
}

object GameScriptGenerationActionFactory : GameActionFactory {

    override fun start(gameName: String, orderName: String) = StartAGameDeclarableAction(gameName, orderName)
    override fun save(gameName: String) = object : TicTacToeScriptGenerationAction() {

        override fun getActionScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeRegardlessOf::saveGame, gameName)
    }

    override fun close(gameName: String) = object : TicTacToeScriptGenerationAction() {

        override fun getActionScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeRegardlessOf::closeGame, gameName)
    }

    override fun load(gameName: String) = object : TicTacToeScriptGenerationAction() {

        override fun getActionScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeRegardlessOf::loadGame, gameName)
    }

    // Move is complex as it can be used both in 'given' and 'when' positions
    override fun move(gameName: String, playerName: String, position: Position) = Move(gameName, playerName, position)

    class Move(gameName: String, playerName: String, position: Position) :
        PlayerMoveDeclarableAction(gameName, playerName, position), IgnoreEnvScriptGenerationAction<TicTacToeGenerationEnvironment> {

        override fun getActionScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeWhen::placeToken, gameName, playerName, position)
    }

    override fun addPlayer(gameName: String, playerName: String) = UnsupportedAction
}

object PlayOrderScriptGenActionFactory : PlayOrderActionFactory {

    override fun create(orderName: String, players: List<String>) = CreatePlayOrderDeclarableAction(orderName, players)
}
