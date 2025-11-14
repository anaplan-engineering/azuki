package com.anaplan.engineering.azuki.tictactoe.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeBehaviours
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeFunctionalElements
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario

@BEH(TicTacToeBehaviours.PlaceToken, TicTacToeFunctionalElements.Game, """
    Place a token
""")
class BEH3 : TicTacToeScenario() {

    @Eac("A player cannot place a token over an existing token")
    fun immutableState() {
        given {
            thereIsAGame(gameA, """
                . | . | .
                . | X | .
                . | . | .
            """)
        }
        then {
            playerCannotPlaceToken(gameA, O, 2 to 2)
        }
    }

    @Eac("When a player places a single token, the board contains that token")
    fun place() {
        given {
            thereIsANewGame(gameA)
        }
        whenever {
            placeToken(gameA, X, 2 to 2)
        }
        then {
            boardHasToken(gameA, X, 2 to 2)
        }
    }

    @Eac("When a player places a token, the board is updated")
    fun turns() {
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

    @Eac("A player cannot move out of turn")
    fun outOfTurn() {
        given {
            thereIsAGame(gameA, """
                . | . | X
                . | . | .
                . | . | .
            """)
        }
        then {
            playerCannotPlaceToken(gameA, X, 1 to 3)
        }
    }

    @Eac("If the play order starts with a cross, only cross can place the first token")
    @KnownBug(Issue("VDM"))
    fun firstTurnCross() {
        given {
            thereIsAPlayOrder(orderA, X, O)
            thereIsANewGame(gameA, orderA)
        }
        then {
            for (row in 1..3) {
                for (col in 1..3) {
                    playerCanPlaceToken(gameA, X, row to col)

                    // NOTE: VDM doesn't support multiple assertions of this kind
                    playerCannotPlaceToken(gameA, O, row to col)
                }
            }
        }
    }
}
