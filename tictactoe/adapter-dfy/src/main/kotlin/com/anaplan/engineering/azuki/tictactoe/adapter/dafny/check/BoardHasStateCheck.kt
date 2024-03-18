package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment

class BoardHasStateCheck(
    private val gameName: String,
    private val moves: MoveMap
) : GetPlayOrderBehaviour(), DafnyCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        TODO("Not yet implemented")
    }
}
