package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment

interface DafnyDeclarationBuilderFactory<D : Declaration> :
    FeDeclarationBuilderFactory<D, DafnyDeclarationBuilder<D>>


abstract class DafnyDeclarationBuilder<D: Declaration>(declaration: D): DeclarationBuilder<D>(declaration) {
    abstract fun build(env: ExecutionEnvironment)
}
