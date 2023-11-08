package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface TicTacToeActionFactory : ActionFactory {
    val game: GameActionFactory
    val playOrder: PlayOrderActionFactory
}

interface PlayOrderActionFactory {
    fun create(orderName: String, players: List<String>): Action
}

interface GameActionFactory {
    fun start(gameName: String, orderName: String): Action
    fun save(gameName: String): Action
    fun close(gameName: String): Action
    fun load(gameName: String): Action
    fun move(gameName: String, playerName: String, position: Position): Action
    fun addPlayer(gameName: String, playerName: String): Action
}
