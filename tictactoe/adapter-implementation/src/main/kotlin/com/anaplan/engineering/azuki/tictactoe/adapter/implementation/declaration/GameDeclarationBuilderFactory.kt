package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.toPlayer
import com.anaplan.engineering.azuki.tictactoe.implementation.Game

class GameDeclarationBuilderFactory : SampleDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): SampleDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        SampleDeclarationBuilder<GameDeclaration>(declaration) {
        override fun build(env: ExecutionEnvironment) {
            val playOrder = env.playOrders[declaration.orderName]!!.map(::toPlayer)
            val prepopulated = declaration.moves.map {
                    (pos, sym) -> (pos.col - 1 to pos.row - 1) to toPlayer(sym).token
            }.toMap()
            env.gameManager.add(declaration.name, Game(3, 3, *playOrder.toTypedArray(), prepopulated = prepopulated))
        }
    }
}
