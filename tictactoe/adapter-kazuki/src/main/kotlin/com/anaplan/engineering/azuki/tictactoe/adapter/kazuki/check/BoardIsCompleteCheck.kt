package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.IsCompleteBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO

class BoardIsCompleteCheck(
    private val gameName: String
) : IsCompleteBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val game = env.get<XO.Game>(gameName)
        return !XO.isUnfinished(game)
    }

}
