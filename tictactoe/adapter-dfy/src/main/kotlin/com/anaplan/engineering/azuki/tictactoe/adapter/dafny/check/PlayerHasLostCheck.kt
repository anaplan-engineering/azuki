package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment

class PlayerHasLostCheck(
    private val gameName: String,
    private val playerName: String,
) : GetPlayOrderBehaviour(), DafnyCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        TODO("Not yet implemented")
    }
}
