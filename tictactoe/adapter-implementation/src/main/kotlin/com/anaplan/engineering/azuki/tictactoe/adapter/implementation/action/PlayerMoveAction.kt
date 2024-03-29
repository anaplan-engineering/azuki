package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.PlayerMoveDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.toPlayer

class PlayerMoveAction(gameName: String, playerName: String, position: Position) :
    PlayerMoveDeclarableAction(gameName, playerName, position), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.withGame(gameName) {
            move(toPlayer(playerName), position.col - 1, position.row - 1)
        }
    }
}
