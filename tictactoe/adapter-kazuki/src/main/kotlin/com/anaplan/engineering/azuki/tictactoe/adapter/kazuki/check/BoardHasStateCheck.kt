package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.kazuki.core.mapping
import com.anaplan.engineering.kazuki.core.mk_

class BoardHasStateCheck(
    private val gameName: String,
    private val moves: MoveMap
) : GetPlayOrderBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val game = env.get<XO.Game>(gameName)
        val expectedBoard = mapping(moves.entries) { (position, playerName) ->
            mk_(position.toKazuki(), playerName.toPlayer())
        }
        return expectedBoard == game.board
    }

}
