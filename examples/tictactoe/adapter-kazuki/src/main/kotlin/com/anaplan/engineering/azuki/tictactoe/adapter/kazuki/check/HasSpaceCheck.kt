package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki

class HasSpaceCheck(
    private val position: Position,
    private val gameName: String
) : PlaceATokenBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) = position.toKazuki() !in env.game(gameName).board.dom
}
