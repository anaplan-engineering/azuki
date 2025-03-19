package com.anaplan.engineering.azuki.tictactoe.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.runner.ToBeDone
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeWhen
import com.anaplan.engineering.azuki.tictactoe.eacs.O
import com.anaplan.engineering.azuki.tictactoe.eacs.X
import com.anaplan.engineering.azuki.tictactoe.eacs.gameA
import org.junit.runners.Parameterized

class MoveOrder(private val testCase: TestCase) : TicTacToeScenario() {

    companion object {

        data class TestCase(val description: String, val moves: TicTacToeWhen.() -> Unit) {
            override fun toString() = description
        }

        private fun testCase(
            description: String,
            knownBug: KnownBug? = null,
            toBeDone: ToBeDone? = null,
            moves: TicTacToeWhen.() -> Unit,
        ): Array<Any> = arrayOf(TestCase(description, moves), *listOfNotNull(knownBug, toBeDone).toTypedArray())

        @Parameterized.Parameters(name = "{0}")
        fun actions() = listOf(
            testCase("1:1 2:2 1:3 1:2") {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 2 to 2)
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 1 to 2)
            },
            testCase("1:1 1:2 1:3 2:2") {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 1 to 2)
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 2 to 2)
            },
            testCase("1:3 2:2 1:1 1:2", KnownBug(Issue("SampleImpl", "FOO-123"), Issue("VDM", "FOO-234"))) {
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 2 to 2)
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 1 to 2)
            },
            testCase("1:3 1:2 1:1 2:2", toBeDone = ToBeDone(Issue("SampleImpl", "BAR-567"))) {
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 1 to 2)
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 2 to 2)
            }
        )
    }

    @AnalysisScenario
    fun equivalentMoveOrder() {
        val testCase = this.testCase
        given {
            thereIsANewGameWithPlayers(gameA, X, O)
        }
        whenever {
            testCase.moves(this)
        }
        then {
            boardHasState(gameA, """
                X | O | X
                . | O | .
                . | . | .
            """)
        }
    }
}
