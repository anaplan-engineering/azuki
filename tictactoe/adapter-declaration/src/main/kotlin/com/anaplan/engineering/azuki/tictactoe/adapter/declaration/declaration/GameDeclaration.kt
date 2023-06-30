package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration

import com.anaplan.engineeering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap

data class GameDeclaration(
    override val name: String,
    val orderName: String,
    val moves: MoveMap = emptyMap(),
    override val standalone: Boolean,
) : Declaration {
}
