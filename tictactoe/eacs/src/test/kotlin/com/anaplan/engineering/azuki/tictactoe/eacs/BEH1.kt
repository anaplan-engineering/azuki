package com.anaplan.engineering.azuki.tictactoe.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.tictactoe.O
import com.anaplan.engineering.azuki.tictactoe.X
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeBehaviours
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeFunctionalElements
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeScenario
import com.anaplan.engineering.azuki.tictactoe.gameA


@BEH(TicTacToeBehaviours.NewGame, TicTacToeFunctionalElements.Game, """
    Start a new game
""")
class BEH1 : TicTacToeScenario() {

    @Eac("When a game is started, neither player has made a move")
    fun noMoves() {
        given {
            thereIsANewGameWithPlayers(gameA, X, O)
        }
        then {
            playerHasMoved(gameA, X, times = 0)
            playerHasMoved(gameA, O, times = 0)
        }
    }

}


