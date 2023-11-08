package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.toPlayer

class CannotPlaceTokenCheck(
    private val gameName: String,
    private val playerName: String,
    private val position: Position
) : PlaceATokenBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            !canMove(toPlayer(playerName), position.col - 1, position.row - 1)
        }
    }
}
