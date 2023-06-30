package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory

interface JGraphTDeclarationBuilderFactory<D : Declaration> :
    FeDeclarationBuilderFactory<D, JGraphTDeclarationBuilder<D>>


abstract class JGraphTDeclarationBuilder<D: Declaration>(declaration: D): DeclarationBuilder<D>(declaration) {

    abstract fun build()
}
