package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import com.anaplan.engineering.azuki.tictactoe.implementation.Token
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.implementation.Player

class PlayOrderDeclarationBuilderFactory : SampleDeclarationBuilderFactory<PlayOrderDeclaration> {

    override val declarationClass = PlayOrderDeclaration::class.java

    override fun create(declaration: PlayOrderDeclaration): SampleDeclarationBuilder<PlayOrderDeclaration> =
        PlayOrderDeclarationBuilder(declaration)

    private class PlayOrderDeclarationBuilder(declaration: PlayOrderDeclaration) :
        SampleDeclarationBuilder<PlayOrderDeclaration>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            env.add(declaration.name, declaration.playOrder.map(::toPlayer))
        }
    }
}

internal fun toPlayer(symbol: String) = when (symbol) {
    "X" -> Player("Cross", Token.Cross)
    "O" -> Player("Circle", Token.Circle)
    else -> TODO("Unrecognised player $symbol")
}
