package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayerMoveCountBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.kazuki.core.card

class PlayerMoveCountCheck(
    private val gameName: String,
    private val playerName: String,
    private val times: Int
) : PlayerMoveCountBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val game = env.get<XO.Game>(gameName)
        val player = playerName.toPlayer()
        return XO.movesForPlayer(game, player).card == times
    }

}
