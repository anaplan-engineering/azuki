package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.EnvironmentBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module

class GameDeclarationBuilderFactory : KazukiDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): KazukiDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        KazukiDeclarationBuilder<GameDeclaration>(declaration) {
        override fun build(builder: EnvironmentBuilder) {
            val board = declaration.moves.map { (position, playerName) ->
                XO_Module.mk_Position(position.row, position.col) to playerName.toPlayer()
            }.toMap()
            builder.declare(declaration.name) { env ->
                XO_Module.mk_Game(board, env.get<XO.PlayOrder>(declaration.orderName))
            }
        }
    }
}
