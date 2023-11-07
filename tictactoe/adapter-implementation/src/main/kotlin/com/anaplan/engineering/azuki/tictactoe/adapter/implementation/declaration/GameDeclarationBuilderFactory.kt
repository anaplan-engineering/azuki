package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import Game
import PlayOrder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class GameDeclarationBuilderFactory : SampleDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): SampleDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        SampleDeclarationBuilder<GameDeclaration>(declaration) {
        override fun build(env: ExecutionEnvironment) {
            val playOrder = env.get<PlayOrder>(declaration.orderName)
            val game = Game.new(3, 3, *playOrder.toTypedArray())
            declaration.moves.map { (pos, sym) -> game.move(toPlayer(sym), pos.col, pos.row) }
            env.add(declaration.name, game)
        }
    }
}
