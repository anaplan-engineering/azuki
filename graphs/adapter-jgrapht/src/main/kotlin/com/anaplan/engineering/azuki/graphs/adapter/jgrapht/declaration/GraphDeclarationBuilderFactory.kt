package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.declaration

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph

class GraphDeclarationBuilderFactory : JGraphTDeclarationBuilderFactory<GraphDeclaration<*>> {

    override val declarationClass = GraphDeclaration::class.java

    override fun create(declaration: GraphDeclaration<*>): JGraphTDeclarationBuilder<GraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

    private class GraphDeclarationBuilder(declaration: GraphDeclaration<*>) :
        JGraphTDeclarationBuilder<GraphDeclaration<*>>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            val graph = SimpleGraph<Any, DefaultEdge>(DefaultEdge::class.java)
            declaration.vertices.map {
                graph.addVertex(it)
            }
            declaration.edges.map {
                // necessary to add vertices before using in an edge
                graph.addVertex(it.first)
                graph.addVertex(it.second)

                graph.addEdge(it.first, it.second)
            }
            env.addGraph(declaration.name, graph)
        }

    }
}
