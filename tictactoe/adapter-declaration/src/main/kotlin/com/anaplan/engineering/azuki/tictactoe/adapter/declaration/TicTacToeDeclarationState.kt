package com.anaplan.engineering.azuki.tictactoe.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.declaration.DeclarationStateFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration

class TicTacToeDeclarationState(): DeclarationState() {
    fun declarePlayOrder(orderName: String, playOrder: List<String>, standalone: Boolean = true) {
        checkForDuplicate(orderName)
        declarations[orderName] = PlayOrderDeclaration(orderName, playOrder, standalone)
    }

    fun declareGame(boardName: String, orderName: String) {
        checkForDuplicate(boardName)
        checkExists(orderName)
        declarations[boardName] = GameDeclaration(boardName, orderName, emptyMap(),true)
    }

    fun playerMove(gameName: String, playerName: String, position: Position) {
        val game = getDeclaration<GameDeclaration>(gameName)
        declarations[gameName] = game.copy(moves = game.moves.plus(position to playerName))
    }

    object Factory : DeclarationStateFactory<TicTacToeDeclarationState> {
        override fun create(): TicTacToeDeclarationState = TicTacToeDeclarationState()
    }
}
