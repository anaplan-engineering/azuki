package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.tictactoe.dsl.verifiableScenario
import org.junit.Test

class TicTacToeScriptGeneratorTest {

    companion object {
        const val orderA = "orderA"

        const val gameA = "gameA"

        const val X = "X"
        const val O = "O"
    }

    @Test
    fun moves() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(
            verifiableScenario {
                given {
                    thereIsAPlayOrder(orderA, O, X)
                    thereIsAGame(gameA, orderA, """
                . | . | .
                . | . | .
                . | . | .
            """)
                }
                whenever {
                    placeToken(gameA, O, 2 to 2)
                    placeToken(gameA, X, 1 to 1)
                }
                then {
                    boardHasState(gameA, """
                X | . | .
                . | O | .
                . | . | .
            """)
                }
            }
        )
    }
}
