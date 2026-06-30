package com.anaplan.engineering.azuki.graphs.adapter.jung.declaration

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.DirectedGraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.UndirectedGraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import com.google.common.graph.NetworkBuilder

class UndirectedGraphDeclarationBuilderFactory : JungDeclarationBuilderFactory<UndirectedGraphDeclaration<*>> {
    override val declarationClass = UndirectedGraphDeclaration::class.java
    override fun create(declaration: UndirectedGraphDeclaration<*>): JungDeclarationBuilder<UndirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)
}

class DirectedGraphDeclarationBuilderFactory : JungDeclarationBuilderFactory<DirectedGraphDeclaration<*>> {
    override val declarationClass = DirectedGraphDeclaration::class.java
    override fun create(declaration: DirectedGraphDeclaration<*>): JungDeclarationBuilder<DirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)
}

private class GraphDeclarationBuilder<T : GraphDeclaration<*>>(declaration: T) :
    JungDeclarationBuilder<T>(declaration) {

    override fun build(env: ExecutionEnvironment) {
        val graph = when (declaration) {
            is UndirectedGraphDeclaration<*> -> NetworkBuilder.undirected()
            is DirectedGraphDeclaration<*> -> NetworkBuilder.directed()
            else -> throw LateDetectUnsupportedActionException("Unknown graph declaration type ${declaration::class}")
        }.build<Any, Pair<*, *>>()
        declaration.vertices.forEach {
            graph.addNode(it)
        }
        declaration.edges.forEach {
            graph.addEdge(it.first, it.second, it)
        }
        env.addGraph(declaration.name, graph)
    }
}
