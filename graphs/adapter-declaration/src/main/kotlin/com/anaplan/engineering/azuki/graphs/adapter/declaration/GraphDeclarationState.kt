package com.anaplan.engineering.azuki.graphs.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.declaration.DeclarationStateFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.DirectedGraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.UndirectedGraphDeclaration

class GraphDeclarationState : DeclarationState() {

    fun declareUndirectedGraph(graphName: String) {
        checkForDuplicate(graphName)
        declarations[graphName] = UndirectedGraphDeclaration<Any>(graphName, standalone = true)
    }

    fun declareDirectedGraph(graphName: String) {
        checkForDuplicate(graphName)
        declarations[graphName] = DirectedGraphDeclaration<Any>(graphName, standalone = true)
    }

    fun <V> addVertexToUndirectedGraph(graphName: String, vertex: V) {
        checkExists(graphName)
        val declaration = getDeclaration<UndirectedGraphDeclaration<V>>(graphName)
        declarations[graphName] = declaration.copy(
            vertices = declaration.vertices + vertex
        )
    }

    fun <V> addEdgeToUndirectedGraph(graphName: String, source: V, target: V) {
        checkExists(graphName)
        val declaration = getDeclaration<UndirectedGraphDeclaration<V>>(graphName)
        declarations[graphName] = declaration.copy(
            edges = declaration.edges + Pair(source, target)
        )
    }

    fun <V> addVertexToDirectedGraph(graphName: String, vertex: V) {
        checkExists(graphName)
        val declaration = getDeclaration<DirectedGraphDeclaration<V>>(graphName)
        declarations[graphName] = declaration.copy(
            vertices = declaration.vertices + vertex
        )
    }

    fun <V> addEdgeToDirectedGraph(graphName: String, source: V, target: V) {
        checkExists(graphName)
        val declaration = getDeclaration<DirectedGraphDeclaration<V>>(graphName)
        declarations[graphName] = declaration.copy(
            edges = declaration.edges + Pair(source, target)
        )
    }

    object Factory : DeclarationStateFactory<GraphDeclarationState> {
        override fun create() = GraphDeclarationState()
    }
}




