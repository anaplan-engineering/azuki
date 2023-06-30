package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.declaration

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration

class CreateGraphDeclarationBuilderFactory : JGraphTDeclarationBuilderFactory<GraphDeclaration<*>> {

    override val declarationClass = GraphDeclaration::class.java

    override fun create(declaration: GraphDeclaration<*>): JGraphTDeclarationBuilder<GraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

    private class GraphDeclarationBuilder(declaration: GraphDeclaration<*>) :
        JGraphTDeclarationBuilder<GraphDeclaration<*>>(declaration) {

        override fun build() {
            TODO("Not yet implemented")
        }

    }
}
