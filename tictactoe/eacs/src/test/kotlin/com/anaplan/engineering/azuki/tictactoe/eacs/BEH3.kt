package com.anaplan.engineering.azuki.tictactoe.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeBehaviours
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeFunctionalElements
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
}
