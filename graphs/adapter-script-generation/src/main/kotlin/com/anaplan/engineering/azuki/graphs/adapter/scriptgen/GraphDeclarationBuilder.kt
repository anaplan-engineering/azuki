package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.DirectedGraphDeclaration
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.dsl.DirectedGraphBlock
import com.anaplan.engineering.azuki.graphs.dsl.UndirectedGraphBlock
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilder

internal class GraphDeclarationBuilder<T : GraphDeclaration<*>>(declaration: T) :
    ScriptGenDeclarationBuilder<T>(declaration) {

    override fun getDeclarationScript(): String {
        val edges = declaration.edges.joinToString("\n") {
            GraphScriptingHelper.scriptifyFunction(
                if (declaration is DirectedGraphDeclaration<*>) DirectedGraphBlock::edge else UndirectedGraphBlock::edge,
                it.first,
                it.second
            )
        }
        val edgeVertices = declaration.edges.flatMap { listOf(it.first, it.second) }.toSet()
        val nonEdgeVertices = declaration.vertices - edgeVertices
        val vertices = nonEdgeVertices.joinToString("\n") {
            GraphScriptingHelper.scriptifyFunction(
                if (declaration is DirectedGraphDeclaration<*>) DirectedGraphBlock::vertex else UndirectedGraphBlock::vertex,
                it
            )
        }
        val constructorName =
            if (declaration is DirectedGraphDeclaration<*>) "thereIsADirectedGraph" else "thereIsAnUndirectedGraph"
        return """
            ${constructorName}("${declaration.name}") {
                $edges
                $vertices
            }
        """
    }
}
