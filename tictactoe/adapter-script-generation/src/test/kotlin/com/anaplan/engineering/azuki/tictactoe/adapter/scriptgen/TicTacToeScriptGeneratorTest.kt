package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationTestHelper
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeBuildableScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.verifiableScenario
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.expect

class TicTacToeScriptGeneratorTest {

    companion object {

        const val orderA = "orderA"

        const val gameA = "gameA"

        const val X = "X"
        const val O = "O"

        val ScenarioScriptingTestUtils = ScriptGenerationTestHelper(generatorFactory = ::TicTacToeScriptGenerator,
            parser = object : SimpleScenarioParser<TicTacToeBuildableScenario>() {
                override val defaultImports: ScenarioParsingContext.() -> Unit = {
                    import("com.anaplan.engineering.azuki.tictactoe.dsl.*")
                    import("com.anaplan.engineering.azuki.tictactoe.*")
                }
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

        assertFalse {
            TicTacToeScriptGenerator().generateScript(scenario).contains("boardHasState")
        }
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

        val lines = TicTacToeScriptGenerator().generateScript(scenario).lines()
        assertFalse { lines.any { "boardHasState" in it } }
        expect(4) { lines.count { "boardHasToken" in it } }
        expect(3) { lines.count { "boardHasSpace" in it } }
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

        val script = TicTacToeScriptGenerator().generateScript(scenario)

        assertContains(script.normalise(), """
            then {
                boardHasState(${triple}gameA${triple},
                ${triple}X | O | X
                . | . | .
                O | . | X$triple)
            }
        """.normalise())
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

        val lines = TicTacToeScriptGenerator().generateScript(scenario).lines()
        assertFalse { lines.any { "boardHasState" in it } }
        expect(6) { lines.count { "boardHasToken" in it } }
        expect(4) { lines.count { "boardHasSpace" in it } }
    }

    private fun String.normalise(): String {
        val noNewlines = replace(Regex("[ \n]+"), " ")
        val noTripleQuoteSpace = noNewlines.replace(Regex(" *${triple} *"), triple)
        val noTrailingComma = noTripleQuoteSpace.replace(Regex(", +\\)"), ")")
        return noTrailingComma
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
}
