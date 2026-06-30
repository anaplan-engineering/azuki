package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState

abstract class PlayerMoveDeclarableAction(
    protected val gameName: String, protected val playerName: String, protected val position: Position
) : PlaceATokenBehaviour(), DeclarableAction<TicTacToeDeclarationState> {

    override fun declare(state: TicTacToeDeclarationState) = state.playerMove(gameName, playerName, position)
}
