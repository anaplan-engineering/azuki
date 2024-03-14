package com.anaplan.engineering.azuki.tictactoe.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario
import com.anaplan.engineering.azuki.tictactoe.eacs.O
import com.anaplan.engineering.azuki.tictactoe.eacs.X
import com.anaplan.engineering.azuki.tictactoe.eacs.gameA
import org.junit.runners.Parameterized

class PlayOrder(private val player1: String, private val player2: String) : TicTacToeScenario() {

    companion object {
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(X, O),
            arrayOf(O, X)
        )
    }

    @AnalysisScenario
    fun hasPlayOrder() {
        // this pattern is often used with parameterized tests, it avoids kotlin's unusual explicit receiver syntax
        // and makes clear where the source of the variables
        val params = this
        given {
            thereIsANewGameWithPlayers(gameA, params.player1, params.player2)
        }
        then {
            gameHasPlayOrder(gameA, params.player1, params.player2)
        }
    }
}
