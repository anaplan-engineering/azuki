package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.GameActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayOrderActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.CreatePlayOrderDeclarableAction

class DafnyActionFactory : TicTacToeActionFactory {
    override val game = DafnyBoardActionFactory
    override val playOrder = DafnyPlayOrderActionFactory
}

object DafnyPlayOrderActionFactory : PlayOrderActionFactory {
    override fun create(orderName: String, players: List<String>) = CreatePlayOrderDeclarableAction(orderName, players)
}

object DafnyBoardActionFactory : GameActionFactory {
    override fun start(gameName: String, orderName: String) = UnsupportedAction
    override fun save(gameName: String) = UnsupportedAction
    override fun close(gameName: String) = UnsupportedAction
    override fun load(gameName: String) = UnsupportedAction
    override fun move(gameName: String, playerName: String, position: Position) = UnsupportedAction
    override fun addPlayer(gameName: String, playerName: String) = UnsupportedAction
}

interface DafnyAction : Action {
    fun act(env: ExecutionEnvironment)
}

val toDafnyAction: (Action) -> DafnyAction = {
    @Suppress("UNCHECKED_CAST")
    it as? DafnyAction ?: throw IllegalArgumentException("Invalid action: $it")
}
