package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action

import com.anaplan.engineering.azuki.tictactoe.adapter.api.StartAGameBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder

open class StartAGameDeclarableAction(private val gameName: String, private val orderName: String) :
    StartAGameBehaviour(), DeclarableAction {

    override fun declare(builder: DeclarationBuilder) {
        builder.declareGame(gameName, orderName)
    }
}
