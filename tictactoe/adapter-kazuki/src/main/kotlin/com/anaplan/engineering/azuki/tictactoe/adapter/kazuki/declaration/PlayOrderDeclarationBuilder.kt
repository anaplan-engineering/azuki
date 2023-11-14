package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.AnimationBuilder

class PlayOrderDeclarationBuilderFactory : KazukiDeclarationBuilderFactory<PlayOrderDeclaration> {

    override val declarationClass = PlayOrderDeclaration::class.java

    override fun create(declaration: PlayOrderDeclaration): KazukiDeclarationBuilder<PlayOrderDeclaration> =
        PlayOrderDeclarationBuilder(declaration)

    private class PlayOrderDeclarationBuilder(declaration: PlayOrderDeclaration) :
        KazukiDeclarationBuilder<PlayOrderDeclaration>(declaration) {

        override fun build(builder: AnimationBuilder) {
        }
    }
}


