package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration

class GameDeclarationBuilderFactory : DafnyDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): DafnyDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        DafnyDeclarationBuilder<GameDeclaration>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            TODO("Not yet implemented")
        }
    }
}
