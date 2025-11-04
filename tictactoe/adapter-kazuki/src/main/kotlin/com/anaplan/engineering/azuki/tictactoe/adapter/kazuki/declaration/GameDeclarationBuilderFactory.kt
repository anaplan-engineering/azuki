package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.EnvironmentBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module
import com.anaplan.engineering.kazuki.core.*

class GameDeclarationBuilderFactory : KazukiDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): KazukiDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        KazukiDeclarationBuilder<GameDeclaration>(declaration) {
        override fun build(builder: EnvironmentBuilder) {
            val board = mapping(declaration.moves.entries) { (position, playerName) ->
                mk_(position.toKazuki(), playerName.toPlayer())
            }
            builder.declare(declaration.name) { env ->
                XO_Module.mk_Game(board, env.get<XO.PlayOrder>(declaration.orderName))
            }
        }
    }
}
