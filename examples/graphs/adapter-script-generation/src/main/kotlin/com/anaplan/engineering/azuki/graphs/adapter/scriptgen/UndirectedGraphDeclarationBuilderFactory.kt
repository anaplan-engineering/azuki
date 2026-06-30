package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.UndirectedGraphDeclaration
import com.anaplan.engineering.azuki.script.generation.NoScriptGenerationEnvironment
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilderFactory

class UndirectedGraphDeclarationBuilderFactory :
    ScriptGenerationDeclarationBuilderFactory<NoScriptGenerationEnvironment, UndirectedGraphDeclaration<*>> {

    override val declarationClass = UndirectedGraphDeclaration::class.java

    override fun create(declaration: UndirectedGraphDeclaration<*>): ScriptGenerationDeclarationBuilder<NoScriptGenerationEnvironment, UndirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)
}
