package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module

class HasPlayOrderCheck(
    private val gameName: String,
    private val playerNames: List<String>,
) : GetPlayOrderBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val game = env.get<XO.Game>(gameName)
        val players = playerNames.map { it.toPlayer() }
        return game.order == XO_Module.mk_PlayOrder(players)
    }

}
