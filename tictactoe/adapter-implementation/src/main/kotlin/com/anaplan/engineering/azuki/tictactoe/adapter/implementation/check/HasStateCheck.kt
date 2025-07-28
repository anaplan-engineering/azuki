package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.pretty
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import org.slf4j.LoggerFactory

class HasStateCheck(
    private val gameName: String,
    private val moves: MoveMap,
) : GetPlayOrderBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withGame(gameName) {
            Log.info("Game:\n$this")
            val actual = mutableMapOf<Position, String>().apply {
                (0 until height).forEach<Int> { y ->
                    (0 until width).forEach<Int> { x ->
                        val token = board[y][x]
                        if (token != null) {
                            put(Position(y + 1, x + 1), token.symbol)
                        }
                    }
                }
            }
            val equals = moves == actual
            if (!equals) {
                Log.error("Actual:\n${actual.pretty(height, width)}")
            }
            equals
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(HasStateCheck::class.java)
    }
}
