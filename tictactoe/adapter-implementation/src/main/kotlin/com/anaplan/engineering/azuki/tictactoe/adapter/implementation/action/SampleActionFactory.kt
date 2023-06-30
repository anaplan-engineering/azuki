package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.GameActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayOrderActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class SampleActionFactory : TicTacToeActionFactory {
    override val game = SampleGameActionFactory
    override val playOrder = SamplePlayOrderActionFactory
}

object SampleGameActionFactory : GameActionFactory {
    override fun start(gameName: String, orderName: String) = StartAGameAction(gameName, orderName)
    override fun save(gameName: String) = UnsupportedAction
    override fun move(gameName: String, playerName: String, position: Position) =
        PlayerMoveAction(gameName, playerName, position)
    override fun addPlayer(gameName: String, playerName: String) = UnsupportedAction
}

object SamplePlayOrderActionFactory : PlayOrderActionFactory {
    override fun create(orderName: String, players: List<String>) = CreatePlayOrderAction(orderName, players)
}

interface SampleAction : Action {
    fun act(env: ExecutionEnvironment)
}
