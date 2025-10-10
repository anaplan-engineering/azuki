package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.DirectedGraphDeclaration
import com.anaplan.engineering.azuki.script.generation.NoEnvScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.NoScriptGenerationEnvironment
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilderFactory

class DirectedGraphDeclarationBuilderFactory :
    ScriptGenerationDeclarationBuilderFactory<NoScriptGenerationEnvironment, DirectedGraphDeclaration<*>> {

    override val declarationClass = DirectedGraphDeclaration::class.java

    override fun create(declaration: DirectedGraphDeclaration<*>): NoEnvScriptGenerationDeclarationBuilder<DirectedGraphDeclaration<*>> =
        GraphDeclarationBuilder(declaration)
}
