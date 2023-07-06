package com.anaplan.engineering.azuki.graphs.adapter.jung.declaration

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import com.google.common.graph.GraphBuilder

class GraphDeclarationBuilderFactory : JungDeclarationBuilderFactory<GraphDeclaration<*>> {

    override val declarationClass = GraphDeclaration::class.java

    override fun create(declaration: GraphDeclaration<*>): JungDeclarationBuilder<GraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

    private class GraphDeclarationBuilder(declaration: GraphDeclaration<*>) :
        JungDeclarationBuilder<GraphDeclaration<*>>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            val graph = GraphBuilder.undirected().build<Any>()
            declaration.vertices.forEach {
                graph.addNode(it)
            }
            declaration.edges.forEach {
                graph.putEdge(it.first, it.second)
            }
            env.addGraph(declaration.name, graph)
        }
    }
}
