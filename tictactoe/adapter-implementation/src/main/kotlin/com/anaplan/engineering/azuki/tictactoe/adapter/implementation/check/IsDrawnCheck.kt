package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.IsDrawnBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class IsDrawnCheck(
    private val gameName: String,
) : IsDrawnBehavior(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            isDrawn()
        }
    }
}
