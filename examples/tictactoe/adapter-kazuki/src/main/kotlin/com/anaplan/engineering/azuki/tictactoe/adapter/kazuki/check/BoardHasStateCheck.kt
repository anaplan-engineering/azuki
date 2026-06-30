package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.kazuki.core.mapping
import com.anaplan.engineering.kazuki.core.mk_

class BoardHasStateCheck(
    private val gameName: String, moves: MoveMap
) : GetPlayOrderBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment) = expectedBoard == env.game(gameName).board

    private val expectedBoard by lazy {
        mapping(moves.entries) { (position, playerName) ->
            mk_(position.toKazuki(), playerName.toPlayer())
        }
    }
}
