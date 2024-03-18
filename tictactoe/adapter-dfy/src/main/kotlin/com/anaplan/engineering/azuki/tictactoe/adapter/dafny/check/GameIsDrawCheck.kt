package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment

class GameIsDrawCheck(
    private val gameName: String
) : GetPlayOrderBehaviour(), DafnyCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        TODO("Not yet implemented")
    }

}
