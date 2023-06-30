package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayerMoveCountBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.toPlayer

class PlayerMoveCountCheck(
    private val gameName: String,
    private val playerName: String,
    private val times: Int
) : PlayerMoveCountBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val moveCount = env.withGame(gameName) {
            playerMoveCount(toPlayer(playerName))
        }
        return moveCount == times
    }
}
