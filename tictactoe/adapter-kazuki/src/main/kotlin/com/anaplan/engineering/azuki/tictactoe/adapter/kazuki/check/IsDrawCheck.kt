package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.IsDrawnBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO

class IsDrawCheck(private val gameName: String) : IsDrawnBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) = XO.isDraw(env.game(gameName))
}
