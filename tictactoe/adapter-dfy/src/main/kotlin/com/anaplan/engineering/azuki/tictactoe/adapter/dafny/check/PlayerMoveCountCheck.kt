package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayerMoveCountBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment

class PlayerMoveCountCheck(
    private val gameName: String,
    private val player: String,
    private val times: Int
) : PlayerMoveCountBehaviour(), DafnyCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        TODO("Not yet implemented")
    }
}
