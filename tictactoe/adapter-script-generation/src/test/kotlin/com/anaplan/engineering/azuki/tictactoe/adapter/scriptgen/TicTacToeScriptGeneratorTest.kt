package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationTesting
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeBuildableScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeOracleScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeQueryScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeVerifiableScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.oracleScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.verifiableScenario
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.expect

class TicTacToeScriptGeneratorTest {

    companion object {

        const val orderA = "orderA"
        const val gameA = "gameA"
        const val X = "X"
        const val O = "O"

        val ScenarioScriptingTestUtils = ScriptGenerationTesting(generator = {
            TicTacToeScriptGeneration.scenario.arbitrary(it,
                { this as? TicTacToeVerifiableScenario },
                { this as? TicTacToeOracleScenario },
                { this as? TicTacToeQueryScenario })
        }, parser = object : SimpleScenarioParser<TicTacToeBuildableScenario>() {

            override val defaultImports: ScenarioParsingContext.() -> Unit = { import(*ticTacToeStandardImports) }
        })
    }

    @Test
    fun boardHasStateNotReconstructedIfUnderspecified() {
        val scenario = verifiableScenario {
            given {
                thereIsANewGame(gameA)
            }
            whenever {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 3 to 1)
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 1 to 2)
                placeToken(gameA, X, 3 to 3)
            }
            then {
                boardHasToken(gameA, X, 1 to 1)
                boardHasSpace(gameA, 2 to 1)
                boardHasToken(gameA, O, 3 to 1)
            }
        }

        val then = TicTacToeScriptGeneration.scenario.verifiable(scenario).then.elements.map { it.render() }
        assertFalse { then.any { "boardHasState" in it } }
        expect(2) { then.count { "boardHasToken" in it } }
        expect(1) { then.count { "boardHasSpace" in it } }
    }

    @Test
    fun boardHasStateNotReconstructedIfSlightlyUnderspecified() {
        val scenario = verifiableScenario {
            given {
                thereIsANewGame(gameA)
            }
            whenever {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 3 to 1)
                placeToken(gameA, X, 2 to 2)
                placeToken(gameA, O, 1 to 2)
                placeToken(gameA, X, 3 to 3)
            }
            then {
                // this is a winning position, but still underspecified
                boardHasToken(gameA, X, 1 to 1)
                boardHasSpace(gameA, 2 to 1)
                boardHasToken(gameA, O, 3 to 1)
                boardHasSpace(gameA, 1 to 2)
                boardHasToken(gameA, X, 2 to 2)
                boardHasSpace(gameA, 3 to 2)
                boardHasToken(gameA, X, 3 to 3)
            }
        }

        val then = TicTacToeScriptGeneration.scenario.verifiable(scenario).then.elements.map { it.render() }
        assertFalse { then.any { "boardHasState" in it } }
        expect(4) { then.count { "boardHasToken" in it } }
        expect(3) { then.count { "boardHasSpace" in it } }
    }

    @Test
    fun boardHasStateIsReconstructedIfFullySpecified() {
        val scenario = verifiableScenario {
            given {
                thereIsANewGame(gameA)
            }
            whenever {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 3 to 1)
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 1 to 2)
                placeToken(gameA, X, 3 to 3)
            }
            then {
                boardHasToken(gameA, X, 1 to 1)
                boardHasToken(gameA, O, 1 to 2)
                boardHasToken(gameA, X, 1 to 3)
                boardHasSpace(gameA, 2 to 1)
                boardHasSpace(gameA, 2 to 2)
                boardHasSpace(gameA, 2 to 3)
                boardHasToken(gameA, O, 3 to 1)
                boardHasSpace(gameA, 3 to 2)
                boardHasToken(gameA, X, 3 to 3)
            }
        }

        expect("""
            boardHasState(${triple}gameA${triple}, ${triple}X | O | X
            . | . | .
            O | . | X
            $triple)
        """.trimIndent()) { TicTacToeScriptGeneration.scenario.verifiable(scenario).then.elements.singleOrNull()?.render() }
    }

    @Test
    fun boardHasStateIsNotReconstructedIfDuplicateChecksAdded() {
        val scenario = verifiableScenario {
            given {
                thereIsANewGame(gameA)
            }
            whenever {
                placeToken(gameA, X, 1 to 1)
                placeToken(gameA, O, 3 to 1)
                placeToken(gameA, X, 1 to 3)
                placeToken(gameA, O, 1 to 2)
                placeToken(gameA, X, 3 to 3)
            }
            then {
                boardHasToken(gameA, X, 1 to 1)
                boardHasToken(gameA, O, 1 to 2)
                boardHasToken(gameA, X, 1 to 3)
                boardHasSpace(gameA, 2 to 1)
                boardHasSpace(gameA, 2 to 2)
                boardHasSpace(gameA, 2 to 3)
                boardHasToken(gameA, O, 3 to 1)
                boardHasSpace(gameA, 3 to 2)
                boardHasToken(gameA, X, 3 to 3)
                boardHasToken(gameA, O, 1 to 1)  // oops!
            }
        }

        val then = TicTacToeScriptGeneration.scenario.verifiable(scenario).then.elements.map { it.render() }
        assertFalse { then.any { "boardHasState" in it } }
        expect(6) { then.count { "boardHasToken" in it } }
        expect(4) { then.count { "boardHasSpace" in it } }
    }

    private val triple = "\"\"\""

    @Test
    fun moves() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(verifiableScenario {
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
                boardHasToken(gameA, X, 1 to 1)
                boardHasToken(gameA, O, 2 to 2)
            }
        })
    }

    @Test
    fun movesAndOnlyTheMoves() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(verifiableScenario {
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
        })
    }

    @Test
    fun exampleOracleScenario() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(oracleScenario {
            generate {
                createPlayOrder("orderA")
                createPlayOrder("orderB")
                createPlayOrder("orderC")
            }
            generate {
                createNewGameFromExistingPlayOrder("gameA")
            }
            generate {
                addMoves("gameA", 4)
            }
            whenever {}
            generate {
                addMoves("gameA", 5)
            }
            verify {
                forAll({ getPositions("gameA") }) { pos ->
                    gameHasToken("gameA", pos.col to pos.row)
                }
                forAll({ getPlayOrder("gameA") }) { player ->
                    forAll({ getPositions("gameA") }) { pos ->
                        playerCanPlaceToken("gameA", player, pos.col to pos.row)
                    }
                }
            }
        })
    }
}
