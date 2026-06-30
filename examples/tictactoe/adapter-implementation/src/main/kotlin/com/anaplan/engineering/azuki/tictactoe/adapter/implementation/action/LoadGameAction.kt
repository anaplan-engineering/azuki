package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class LoadGameAction(
    private val gameName: String,
) : SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.gameManager.load(gameName)
    }

    override val behavior = -1
}
