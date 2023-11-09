package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.IsDrawnBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class IsDrawnCheck(
    private val gameName: String,
) : IsDrawnBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            isDrawn()
        }
    }
}
