package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class CloseGameAction(
    private val gameName: String,
) : SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.gameManager.close(gameName)
    }

    override val behavior = -1
}
