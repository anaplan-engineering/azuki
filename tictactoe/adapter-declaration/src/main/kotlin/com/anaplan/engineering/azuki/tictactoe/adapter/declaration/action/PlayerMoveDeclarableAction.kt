package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder

abstract class PlayerMoveDeclarableAction(
    protected val gameName: String,
    protected val playerName: String,
    protected val position: Position
) :
    PlaceATokenBehaviour(), DeclarableAction {

    override fun declare(builder: DeclarationBuilder) = builder.playerMove(gameName, playerName, position)
}
