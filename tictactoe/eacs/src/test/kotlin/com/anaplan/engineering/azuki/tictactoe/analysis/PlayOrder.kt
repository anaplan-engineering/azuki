package com.anaplan.engineering.azuki.tictactoe.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario
import com.anaplan.engineering.azuki.tictactoe.eacs.O
import com.anaplan.engineering.azuki.tictactoe.eacs.X
import com.anaplan.engineering.azuki.tictactoe.eacs.gameA

class PlayOrder : TicTacToeScenario() {

    @AnalysisScenario
    fun hasPlayOrder() {
        given {
            thereIsANewGameWithPlayers(gameA, X, O)
        }
        then {
            gameHasPlayOrder(gameA, X, O)
        }
    }
}
