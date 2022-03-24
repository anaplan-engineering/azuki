package com.anaplan.engineering.azuki.tictactoe.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario
import com.anaplan.engineering.azuki.tictactoe.eacs.O
import com.anaplan.engineering.azuki.tictactoe.eacs.X
import com.anaplan.engineering.azuki.tictactoe.eacs.gameA


class SavingAGame : TicTacToeScenario() {

    @AnalysisScenario
    fun savingAGame() {
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

}


