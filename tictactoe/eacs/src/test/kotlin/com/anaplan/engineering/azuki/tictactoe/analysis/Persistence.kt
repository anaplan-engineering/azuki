package com.anaplan.engineering.azuki.tictactoe.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario
import com.anaplan.engineering.azuki.tictactoe.eacs.O
import com.anaplan.engineering.azuki.tictactoe.eacs.X
import com.anaplan.engineering.azuki.tictactoe.eacs.gameA


class Persistence : TicTacToeScenario() {

    @AnalysisScenario
    fun savingAGame_empty() {
        given {
            thereIsANewGameWithPlayers(gameA, X, O)
        }
        then {
            playerHasMoved(gameA, X, times = 0)
            playerHasMoved(gameA, O, times = 0)
        }
        regardlessOf {
            saveGame(gameA)
        }
    }

    @AnalysisScenario
    fun reopenAGame_empty() {
        given {
            thereIsANewGameWithPlayers(gameA, X, O)
        }
        then {
            playerHasMoved(gameA, X, times = 0)
            playerHasMoved(gameA, O, times = 0)
        }
        regardlessOf {
            saveGame(gameA)
            closeGame(gameA)
            loadGame(gameA)
        }
    }

    @AnalysisScenario
    fun savingAGame_populated() {
        given {
            thereIsAGame(gameA, """
                X | X | O
                . | . | .
                X | . | O
            """)
        }
        then {
            playerHasMoved(gameA, X, times = 3)
            playerHasMoved(gameA, O, times = 2)
        }
        regardlessOf {
            saveGame(gameA)
        }
    }

    @AnalysisScenario
    fun reopenAGame_populated() {
        given {
            thereIsAGame(gameA, """
                X | X | O
                . | . | .
                X | . | O
            """)
        }
        then {
            playerHasMoved(gameA, X, times = 3)
            playerHasMoved(gameA, O, times = 2)
        }
        regardlessOf {
            saveGame(gameA)
            closeGame(gameA)
            loadGame(gameA)
        }
    }

}


