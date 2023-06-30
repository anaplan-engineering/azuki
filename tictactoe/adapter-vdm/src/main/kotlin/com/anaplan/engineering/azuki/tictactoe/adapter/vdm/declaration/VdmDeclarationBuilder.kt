package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.declaration

import com.anaplan.engineeering.azuki.declaration.Declaration
import com.anaplan.engineeering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineeering.azuki.declaration.FeDeclarationBuilderFactory
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.VdmDeclaration
import com.anaplan.engineering.azuki.vdm.toVdmName
import com.anaplan.engineering.vdmanimation.api.Import

interface VdmDeclarationBuilderFactory<D : Declaration> : FeDeclarationBuilderFactory<D, VdmDeclarationBuilder<D>>

abstract class VdmDeclarationBuilder<D : Declaration>(declaration: D) : DeclarationBuilder<D>(declaration) {

    open fun imports(builder: DefaultModuleBuilder): Set<Import> = emptySet()

    open fun declarations(builder: DefaultModuleBuilder, container: Declaration? = null): List<VdmDeclaration> =
        emptyList()

    open fun build(builder: DefaultModuleBuilder) = builder.extend(
        requiredImports = imports(builder),
        topLevelDeclarations = declarations(builder),
        setters = mapOf(declaration.name to { v: String -> "${declaration.vdmName()} := $v" }),
        getters = mapOf(declaration.name to declaration.vdmName())
    )

    // getters that are used within the definition and which may be used by other objects within the definition
    open fun nestedGetters(): Map<String, String> = emptyMap()
}

internal fun Declaration.vdmName() = toVdmName(name)
