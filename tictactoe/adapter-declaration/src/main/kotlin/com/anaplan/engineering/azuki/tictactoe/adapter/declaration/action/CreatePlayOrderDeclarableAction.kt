package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action

import com.anaplan.engineering.azuki.tictactoe.adapter.api.CreatePlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.StartAGameBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder

open class CreatePlayOrderDeclarableAction(private val orderName: String, private val players: List<String>) :
    CreatePlayOrderBehaviour(), DeclarableAction {

    override fun declare(builder: DeclarationBuilder) {
        builder.declarePlayOrder(orderName, players)
    }
}
