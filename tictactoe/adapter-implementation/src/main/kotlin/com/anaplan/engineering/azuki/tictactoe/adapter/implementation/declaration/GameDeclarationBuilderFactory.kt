package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.implementation.Game
import com.anaplan.engineering.azuki.tictactoe.implementation.PlayOrder

class GameDeclarationBuilderFactory : SampleDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): SampleDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        SampleDeclarationBuilder<GameDeclaration>(declaration) {
        override fun build(env: ExecutionEnvironment) {
            val playOrder = env.get<PlayOrder>(declaration.orderName)
            val game = Game.new(3, 3, *playOrder.toTypedArray())
            declaration.moves.map { (pos, sym) -> game.move(toPlayer(sym), pos.col - 1, pos.row - 1) }
            env.add(declaration.name, game)
        }
    }
}
