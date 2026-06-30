package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module

class HasPlayOrderCheck(
    private val gameName: String,
    private val playerNames: List<String>
) : GetPlayOrderBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) =
        env.game(gameName).order == XO_Module.as_PlayOrder(playerNames.map { it.toPlayer() })
}
