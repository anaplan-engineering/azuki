package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory

interface ScriptGenerationDeclarationBuilderFactory<E : ScriptGenerationEnvironment, D : Declaration> :
    FeDeclarationBuilderFactory<D, ScriptGenerationDeclarationBuilder<E, D>>

/**
 * Handles building a script fragment for a particular declaration.
 */
abstract class ScriptGenerationDeclarationBuilder<E : ScriptGenerationEnvironment, D : Declaration>(declaration: D) :
    DeclarationBuilder<D>(declaration) {

    /**
     * Generates the declaration script.
     * The given generation environment may be referenced or updated with information from the declaration.
     */
    abstract fun getDeclarationScript(environment: E): String

}

/**
 * Simplified declaration builder stub for when we don't need an environment.
 */
abstract class NoEnvScriptGenerationDeclarationBuilder<D : Declaration>(declaration: D) :
    ScriptGenerationDeclarationBuilder<NoScriptGenerationEnvironment, D>(declaration) {

    abstract fun getDeclarationScript(): String

    override fun getDeclarationScript(environment: NoScriptGenerationEnvironment) = getDeclarationScript()
}
