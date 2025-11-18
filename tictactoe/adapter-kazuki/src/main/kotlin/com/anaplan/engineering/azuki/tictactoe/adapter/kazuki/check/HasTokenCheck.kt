package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer

class HasTokenCheck(
    private val gameName: String,
    private val position: Position,
    private val playerName: String
) : PlaceATokenBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) =
        env.game(gameName).board[position.toKazuki()] == playerName.toPlayer()
}
