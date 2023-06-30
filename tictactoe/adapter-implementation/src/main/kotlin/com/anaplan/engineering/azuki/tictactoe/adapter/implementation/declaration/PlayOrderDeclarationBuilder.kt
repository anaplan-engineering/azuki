package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import Player
import Token
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class PlayOrderDeclarationBuilder(override val declaration: PlayOrderDeclaration) :
    SampleDeclarationBuilder<PlayOrderDeclaration> {

    override fun build(env: ExecutionEnvironment) {
        env.add(declaration.name, declaration.playOrder.map(::toPlayer))
    }
}

internal fun toPlayer(symbol: String) = when(symbol) {
    "X" -> Player("Cross", Token.Cross)
    "O" -> Player("Circle", Token.Circle)
    else -> TODO("Unrecognised player $symbol")
}
