package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.DirectedGraphDeclaration
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilderFactory

class DirectedGraphDeclarationBuilderFactory : ScriptGenDeclarationBuilderFactory<DirectedGraphDeclaration<*>> {

    override val declarationClass = DirectedGraphDeclaration::class.java

    override fun create(declaration: DirectedGraphDeclaration<*>): ScriptGenDeclarationBuilder<DirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)
}
