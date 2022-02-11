package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.declaration

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.Declaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName
import com.anaplan.engineering.azuki.vdm.VdmDeclaration
import com.anaplan.engineering.vdmanimation.api.Import

interface VdmDeclarationBuilder<T : Declaration> {
    val declaration: T

    fun imports(builder: DefaultModuleBuilder): Set<Import>

    fun declarations(builder: DefaultModuleBuilder, container: Declaration? = null): List<VdmDeclaration>

    fun build(builder: DefaultModuleBuilder) = builder.extend(
            requiredImports = imports(builder),
            topLevelDeclarations = declarations(builder),
            setters = mapOf(declaration.name to { v: String -> "${declaration.vdmName()} := $v" }),
            getters = mapOf(declaration.name to declaration.vdmName())
    )

    // getters that are used within the definition and which may be used by other objects within the definition
    fun nestedGetters(): Map<String, String> = emptyMap()

}

internal fun Declaration.vdmName() = toVdmName(name)

internal fun <T : Declaration> createVdmDeclarationBuilder(declaration: T) =
    when (declaration) {
        is PlayOrderDeclaration -> PlayOrderDeclarationBuilder(declaration)
        is GameDeclaration -> GameDeclarationBuilder(declaration)
        else -> throw IllegalArgumentException("Unknown declaration: $declaration")
    }
