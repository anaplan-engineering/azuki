package com.anaplan.engineering.azuki.graphs.adapter.jung.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

interface JungDeclarationBuilderFactory<D : Declaration> :
    FeDeclarationBuilderFactory<D, JungDeclarationBuilder<D>>


abstract class JungDeclarationBuilder<D: Declaration>(declaration: D): DeclarationBuilder<D>(declaration) {

    abstract fun build(env: ExecutionEnvironment)
}
