package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check.HasSpaceCheck.Companion
import org.slf4j.LoggerFactory

class HasTokenCheck(
    private val gameName: String,
    private val player: String,
    private val position: Position,
) : GetPlayOrderBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            val token = tokenAt(x = position.col - 1, y = position.row - 1)?.symbol
            val isValid = token == player
            if (!isValid) Log.error("Expected {} at {}, found {}", player, position, token)
            isValid
        }
    }

    companion object {

        private val Log = LoggerFactory.getLogger(HasStateCheck::class.java)
    }
}
