package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayerMoveCountBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.toNat

class PlayerMoveCountCheck(
    private val gameName: String,
    private val playerName: String,
    private val times: Int
) : PlayerMoveCountBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) =
        XO.movesForPlayer(env.game(gameName), playerName.toPlayer()).card == times.toNat()
}
