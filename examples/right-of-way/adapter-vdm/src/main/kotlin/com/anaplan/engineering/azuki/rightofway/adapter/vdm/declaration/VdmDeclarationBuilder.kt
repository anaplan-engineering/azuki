package com.anaplan.engineering.azuki.rightofway.adapter.vdm.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.vdm.VdmDeclaration
import com.anaplan.engineering.azuki.vdm.toVdmName
import com.anaplan.engineering.vdmanimation.api.Import

interface VdmDeclarationBuilderFactory<D : Declaration> : FeDeclarationBuilderFactory<D, VdmDeclarationBuilder<D>>

abstract class VdmDeclarationBuilder<D : Declaration>(declaration: D) : DeclarationBuilder<D>(declaration) {

    open fun imports(builder: RightOfWayModuleBuilder): Set<Import> = emptySet()

    open fun declarations(builder: RightOfWayModuleBuilder, container: Declaration? = null): List<VdmDeclaration> =
        emptyList()

    // getters / setters inject Kotlin representation as VDM equivalent/accessible ones.
    open fun build(builder: RightOfWayModuleBuilder) = builder.extend(
        requiredImports = imports(builder),
        topLevelDeclarations = declarations(builder),
        setters = mapOf(declaration.name to { v: String -> "${declaration.vdmName()} := $v" }),
        getters = mapOf(declaration.name to declaration.vdmName()) + nestedGetters(),
    )

    // getters that are used within the definition and which may be used by other objects within the definition
    open fun nestedGetters(): Map<String, String> = emptyMap()
}

internal fun Declaration.vdmName() = toVdmName(name)
