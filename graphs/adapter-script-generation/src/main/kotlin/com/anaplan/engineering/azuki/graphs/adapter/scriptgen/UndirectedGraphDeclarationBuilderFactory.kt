package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.UndirectedGraphDeclaration
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilderFactory

class UndirectedGraphDeclarationBuilderFactory : ScriptGenDeclarationBuilderFactory<UndirectedGraphDeclaration<*>> {

    override val declarationClass = UndirectedGraphDeclaration::class.java

    override fun create(declaration: UndirectedGraphDeclaration<*>): ScriptGenDeclarationBuilder<UndirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)
}
