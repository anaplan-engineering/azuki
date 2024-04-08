package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.declaration

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.DirectedGraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.UndirectedGraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder

class UndirectedGraphDeclarationBuilderFactory : JGraphTDeclarationBuilderFactory<UndirectedGraphDeclaration<*>> {

    override val declarationClass = UndirectedGraphDeclaration::class.java

    override fun create(declaration: UndirectedGraphDeclaration<*>): JGraphTDeclarationBuilder<UndirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

}

class DirectedGraphDeclarationBuilderFactory : JGraphTDeclarationBuilderFactory<DirectedGraphDeclaration<*>> {

    override val declarationClass = DirectedGraphDeclaration::class.java

    override fun create(declaration: DirectedGraphDeclaration<*>): JGraphTDeclarationBuilder<DirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

}

private class GraphDeclarationBuilder<T : GraphDeclaration<*>>(declaration: T) :
    JGraphTDeclarationBuilder<T>(declaration) {

    override fun build(env: ExecutionEnvironment) {
        val graphBuilder = when (declaration) {
            is UndirectedGraphDeclaration<*> -> GraphTypeBuilder.undirected<Any, DefaultEdge>()
                .edgeClass(DefaultEdge::class.java)

            is DirectedGraphDeclaration<*> -> GraphTypeBuilder.directed<Any, DefaultEdge>()
                .edgeClass(DefaultEdge::class.java)

            else -> throw LateDetectUnsupportedActionException("Unkown graph declaration type ${declaration::class}")
        }
        val graph = if (declaration.edges.any { it.first == it.second }) {
            graphBuilder.allowingSelfLoops(true).buildGraph()
        } else {
            graphBuilder.buildGraph()
        }
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
