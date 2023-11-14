package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.AnimationBuilder
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module

class GameDeclarationBuilderFactory : KazukiDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): KazukiDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        KazukiDeclarationBuilder<GameDeclaration>(declaration) {
        override fun build(builder: AnimationBuilder) {
            builder.declare {
                XO_Module.mk_Game()
            }
        }
    }
}
