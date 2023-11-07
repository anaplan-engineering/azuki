package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.toPlayer

class HasPlayOrderCheck(
    private val gameName: String,
    private val players: List<String>,
) : GetPlayOrderBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            playOrder == players.map { toPlayer(it) }
        }
    }
}
