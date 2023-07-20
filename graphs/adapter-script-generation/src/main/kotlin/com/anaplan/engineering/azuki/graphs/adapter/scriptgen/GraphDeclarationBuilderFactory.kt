package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration
import com.anaplan.engineering.azuki.graphs.dsl.GraphBlock
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilderFactory

class GraphDeclarationBuilderFactory : ScriptGenDeclarationBuilderFactory<GraphDeclaration<*>> {

    override val declarationClass = GraphDeclaration::class.java

    override fun create(declaration: GraphDeclaration<*>): ScriptGenDeclarationBuilder<GraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)

    private class GraphDeclarationBuilder(declaration: GraphDeclaration<*>) :
        ScriptGenDeclarationBuilder<GraphDeclaration<*>>(declaration) {

        override fun getDeclarationScript(): String {
            val edges = declaration.edges.joinToString("\n") {
                GraphScriptingHelper.scriptifyFunction(
                    GraphBlock::edge,
                    it.first,
                    it.second
                )
            }
            val edgeVertices = declaration.edges.flatMap { listOf(it.first, it.second) }.toSet()
            val nonEdgeVertices = declaration.vertices - edgeVertices
            val vertices = nonEdgeVertices.joinToString("\n") {
                GraphScriptingHelper.scriptifyFunction(
                    GraphBlock::vertex,
                    it
                )
            }
            return """
                    thereIsAGraph("${declaration.name}") {
                        $edges
                        $vertices
                    }
                """
        }
    }
}
