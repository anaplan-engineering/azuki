package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.declaration

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph

class CreateGraphDeclarationBuilderFactory : JGraphTDeclarationBuilderFactory<GraphDeclaration<*>> {

    override val declarationClass = GraphDeclaration::class.java

    override fun create(declaration: GraphDeclaration<*>): JGraphTDeclarationBuilder<GraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

    private class GraphDeclarationBuilder(declaration: GraphDeclaration<*>) :
        JGraphTDeclarationBuilder<GraphDeclaration<*>>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            env.addGraph(declaration.name, SimpleGraph<Any, DefaultEdge>(DefaultEdge::class.java))
        }

    }
}
