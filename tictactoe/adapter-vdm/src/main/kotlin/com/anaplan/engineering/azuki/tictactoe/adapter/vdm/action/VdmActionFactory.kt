package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.action

import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.GameActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayOrderActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.CreatePlayOrderDeclarableAction

class VdmActionFactory : TicTacToeActionFactory {
    override val game = VdmBoardActionFactory
    override val playOrder = VdmPlayOrderActionFactory
}

object VdmPlayOrderActionFactory : PlayOrderActionFactory {
    override fun create(orderName: String, players: List<String>) = CreatePlayOrderDeclarableAction(orderName, players)
}

object VdmBoardActionFactory : GameActionFactory {
    override fun start(gameName: String, orderName: String) = StartAGameAction(gameName, orderName)
    override fun save(gameName: String) = UnsupportedAction
    override fun close(gameName: String) = UnsupportedAction
    override fun load(gameName: String) = UnsupportedAction
    override fun move(gameName: String, playerName: String, position: Position) =
        PlayerMoveAction(gameName, playerName, position)
    override fun addPlayer(gameName: String, playerName: String) = UnsupportedAction
}
