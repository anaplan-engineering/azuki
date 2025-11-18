package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.canMove
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.toPlayer

class CanPlaceTokenCheck(
    private val gameName: String,
    private val playerName: String,
    private val position: Position,
    private val expected: Boolean
) : PlaceATokenBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment) = env.withGame(gameName) { expected == canMove(playerName, position) }
}
