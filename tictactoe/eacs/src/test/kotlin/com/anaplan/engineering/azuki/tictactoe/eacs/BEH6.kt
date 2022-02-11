package com.anaplan.engineering.azuki.tictactoe.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeBehaviours
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeFunctionalElements
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario

@BEH(TicTacToeBehaviours.GameEnd, TicTacToeFunctionalElements.Game, """
    Complete a game
""")
class BEH6(implementation: TicTacToeImplementation) : TicTacToeScenario(implementation) {

    @Eac("A player can win by completing a line of three tokens")
    fun columnWin() {
        given {
            thereIsAGame(gameA, """
                X | X | O
                . | . | .
                X | . | O
            """)
        }
        whenever {
            placeToken(gameA, O, 2 to 3)
        }
        then {
            boardIsComplete(gameA)
            playerHasWon(gameA, O)
            playerHasLost(gameA, X)
        }
    }

    @Eac("It is possible for the board to be complete without either player having won")
    fun draw() {
        given {
            thereIsAGame(gameA, """
            X | O | O
            O | X | X
            X | X | O
        """)
        }
        then {
            boardIsComplete(gameA)
            gameIsDraw(gameA)
        }
    }
}
