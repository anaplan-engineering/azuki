package com.anaplan.engineering.azuki.graphs.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.declaration.DeclarationStateFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration

class GraphDeclarationState : DeclarationState() {

    fun declareGraph(graphName: String) {
        checkForDuplicate(graphName)
        declarations[graphName] = GraphDeclaration<Any>(graphName, standalone = true)
    }

    fun <V> addVertex(graphName: String, vertex: V) {
        checkExists(graphName)
        val declaration = getDeclaration<GraphDeclaration<V>>(graphName)
        declarations[graphName] = declaration.copy(
            vertices = declaration.vertices + vertex
        )
    }

    fun <V> addEdge(graphName: String, source: V, target: V) {
        checkExists(graphName)
        val declaration = getDeclaration<GraphDeclaration<V>>(graphName)
        declarations[graphName] = declaration.copy(
            edges = declaration.edges + Pair(source, target)
        )
    }

    object Factory: DeclarationStateFactory<GraphDeclarationState> {
        override fun create() = GraphDeclarationState()
    }
}




