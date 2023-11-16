package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.EnvironmentBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.toPlayer
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module

class PlayOrderDeclarationBuilderFactory : KazukiDeclarationBuilderFactory<PlayOrderDeclaration> {

    override val declarationClass = PlayOrderDeclaration::class.java

    override fun create(declaration: PlayOrderDeclaration): KazukiDeclarationBuilder<PlayOrderDeclaration> =
        PlayOrderDeclarationBuilder(declaration)

    private class PlayOrderDeclarationBuilder(declaration: PlayOrderDeclaration) :
        KazukiDeclarationBuilder<PlayOrderDeclaration>(declaration) {

        override fun build(builder: EnvironmentBuilder) {
            builder.declare(declaration.name) {
                XO_Module.mk_PlayOrder(declaration.playOrder.map { it.toPlayer() })
            }
        }
    }
}


