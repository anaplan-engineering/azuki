package com.anaplan.engineering.azuki.graphs.adapter.jung.declaration

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import com.google.common.graph.GraphBuilder
import com.google.common.graph.NetworkBuilder

class GraphDeclarationBuilderFactory : JungDeclarationBuilderFactory<GraphDeclaration<*>> {

    override val declarationClass = GraphDeclaration::class.java

    override fun create(declaration: GraphDeclaration<*>): JungDeclarationBuilder<GraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

    private class GraphDeclarationBuilder(declaration: GraphDeclaration<*>) :
        JungDeclarationBuilder<GraphDeclaration<*>>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            val graph = NetworkBuilder.undirected().build<Any, Pair<*, *>>()
            declaration.vertices.forEach {
                graph.addNode(it)
            }
            declaration.edges.forEach {
                graph.addEdge(it.first, it.second, it)
            }
            env.addGraph(declaration.name, graph)
        }
    }
}
