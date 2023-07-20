package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory

interface ScriptGenDeclarationBuilderFactory<D : Declaration> :
    FeDeclarationBuilderFactory<D, ScriptGenDeclarationBuilder<D>>


abstract class ScriptGenDeclarationBuilder<D : Declaration>(declaration: D) : DeclarationBuilder<D>(declaration) {

    abstract fun getDeclarationScript(): String

}
