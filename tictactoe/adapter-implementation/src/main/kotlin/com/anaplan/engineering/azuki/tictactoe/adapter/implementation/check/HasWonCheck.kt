package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.HasWonBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.toPlayer

class HasWonCheck(
    private val gameName: String,
    private val playerName: String,
) : HasWonBehavior(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            hasWon(toPlayer(playerName))
        }
    }
}
