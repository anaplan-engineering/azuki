package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.toDafnyPlayOrder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration

class PlayOrderDeclarationBuilderFactory : DafnyDeclarationBuilderFactory<PlayOrderDeclaration> {

    override val declarationClass = PlayOrderDeclaration::class.java

    override fun create(declaration: PlayOrderDeclaration): DafnyDeclarationBuilder<PlayOrderDeclaration> =
        PlayOrderDeclarationBuilder(declaration)

    private class PlayOrderDeclarationBuilder(declaration: PlayOrderDeclaration) :
        DafnyDeclarationBuilder<PlayOrderDeclaration>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            env.add(declaration.name, toDafnyPlayOrder(declaration.playOrder))
        }

    }
}
