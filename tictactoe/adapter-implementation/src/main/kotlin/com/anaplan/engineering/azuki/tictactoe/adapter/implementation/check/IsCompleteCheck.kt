package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.IsCompleteBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class IsCompleteCheck(
    private val gameName: String,
) : IsCompleteBehavior(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            isComplete()
        }
    }
}
