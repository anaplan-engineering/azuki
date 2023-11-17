package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.action

import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.PlayerMoveDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module

class PlayerMoveAction(
    gameName: String,
    playerName: String,
    position: Position
) : PlayerMoveDeclarableAction(gameName, playerName, position), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        val game = env.get<XO.Game>(gameName)
        env.set(gameName, XO.move(game, playerName.toPlayer(), position.toKazuki()))
    }
}
