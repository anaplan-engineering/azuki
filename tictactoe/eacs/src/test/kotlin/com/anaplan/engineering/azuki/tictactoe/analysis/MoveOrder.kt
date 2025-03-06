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

        data class TestCase(val moves: TicTacToeWhen.() -> Unit)

        @Parameterized.Parameters
        fun actions() = listOf(
            TestCase {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 2 to 2)
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 1 to 2)
            },
            TestCase {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 1 to 2)
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 2 to 2)
            }
        ).map { arrayOf(it) } + listOf(
            arrayOf(
                TestCase {
                    placeToken(gameA, X, 1 to 3)
                    placeToken(gameA, O, 2 to 2)
                    placeToken(gameA, X, 1 to 1)
                    placeToken(gameA, O, 1 to 2)
                },
                KnownBug(Issue("SampleImpl", "FOO-123"), Issue("VDM", "FOO-234"))
            ),
            arrayOf(
                TestCase {
                    placeToken(gameA, X, 1 to 3)
                    placeToken(gameA, O, 1 to 2)
                    placeToken(gameA, X, 1 to 1)
                    placeToken(gameA, O, 2 to 2)
                }, ToBeDone(Issue("SampleImpl", "BAR-567"))
            )
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
