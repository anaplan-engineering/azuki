package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import org.slf4j.LoggerFactory

class HasSpaceCheck(
    private val gameName: String,
    private val position: Position,
) : GetPlayOrderBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            val token = tokenAt(x = position.col - 1, y = position.row - 1)?.symbol
            val isSpace = token == null
            if (!isSpace) Log.error("Expected space at {}, found {}", position, token)
            isSpace
        }
    }

    companion object {

        private val Log = LoggerFactory.getLogger(HasStateCheck::class.java)
    }
}
