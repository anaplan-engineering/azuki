package com.anaplan.engineering.azuki.graphs.adapter.jung.declaration

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import com.google.common.graph.GraphBuilder

class CreateGraphDeclarationBuilderFactory : JungDeclarationBuilderFactory<GraphDeclaration<*>> {

    override val declarationClass = GraphDeclaration::class.java

    override fun create(declaration: GraphDeclaration<*>): JungDeclarationBuilder<GraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

    private class GraphDeclarationBuilder(declaration: GraphDeclaration<*>) :
        JungDeclarationBuilder<GraphDeclaration<*>>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            env.addGraph(declaration.name, GraphBuilder.undirected().build<Any>())
        }
    }
}
