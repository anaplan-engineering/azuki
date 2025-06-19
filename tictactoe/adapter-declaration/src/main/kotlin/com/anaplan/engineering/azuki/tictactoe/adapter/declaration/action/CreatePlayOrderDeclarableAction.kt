package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.CreatePlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState

open class CreatePlayOrderDeclarableAction(protected val orderName: String, protected val players: List<String>) :
    CreatePlayOrderBehaviour(), DeclarableAction<TicTacToeDeclarationState> {

    override fun declare(state: TicTacToeDeclarationState) = state.declarePlayOrder(orderName, players)
}
