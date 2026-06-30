package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class SaveGameAction(
    private val gameName: String,
) : SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.gameManager.save(gameName)
    }

    override val behavior = -1
}
