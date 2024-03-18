package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment

class PlayerCannotPlaceTokenCheck(
    private val gameName: String,
    private val playerName: String,
    private val position: Position
) : PlaceATokenBehaviour(), DafnyCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        TODO("Not yet implemented")
    }
}
