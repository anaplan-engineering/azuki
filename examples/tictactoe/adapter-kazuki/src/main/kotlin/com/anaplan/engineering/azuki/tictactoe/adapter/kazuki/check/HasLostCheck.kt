package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.HasWonBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO

class HasLostCheck(
    private val gameName: String,
    private val playerName: String
) : HasWonBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) = XO.hasLost(env.game(gameName), playerName.toPlayer())
}
