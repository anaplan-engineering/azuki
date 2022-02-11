package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.Declaration

data class GameDeclaration(
    override val name: String,
    val orderName: String,
    val moves: MoveMap = emptyMap(),
    override val standalone: Boolean,
) : Declaration {
}
