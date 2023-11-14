package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.GameActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayOrderActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.AnimationBuilder

class KazukiActionFactory : TicTacToeActionFactory {
    override val game = KazukiGameActionFactory
    override val playOrder = KazukiPlayOrderActionFactory
}

object KazukiGameActionFactory : GameActionFactory {
    override fun start(gameName: String, orderName: String) = UnsupportedAction
    override fun save(gameName: String) = UnsupportedAction
    override fun close(gameName: String) = UnsupportedAction
    override fun load(gameName: String) = UnsupportedAction
    override fun move(gameName: String, playerName: String, position: Position) =
        UnsupportedAction

    override fun addPlayer(gameName: String, playerName: String) = UnsupportedAction
}

object KazukiPlayOrderActionFactory : PlayOrderActionFactory {
    override fun create(orderName: String, players: List<String>) = UnsupportedAction
}

interface KazukiAction : Action {
    fun act(env: AnimationBuilder)
}
