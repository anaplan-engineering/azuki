package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.kazuki.core.ConditionFailure

class CanPlaceTokenCheck(
    private val gameName: String,
    private val playerName: String,
    private val position: Position,
    private val expected: Boolean
) : PlaceATokenBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) =
        expected == XO.canMove(env.game(gameName), playerName.toPlayer(), position.toKazuki())
}
