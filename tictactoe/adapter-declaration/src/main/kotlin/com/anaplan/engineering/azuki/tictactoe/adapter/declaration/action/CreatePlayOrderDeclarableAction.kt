package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action

import com.anaplan.engineering.azuki.tictactoe.adapter.api.CreatePlayOrderBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder

open class CreatePlayOrderDeclarableAction(protected val orderName: String, protected val players: List<String>) :
    CreatePlayOrderBehavior(), DeclarableAction {

    override fun declare(builder: DeclarationBuilder) {
        builder.declarePlayOrder(orderName, players)
    }
}
