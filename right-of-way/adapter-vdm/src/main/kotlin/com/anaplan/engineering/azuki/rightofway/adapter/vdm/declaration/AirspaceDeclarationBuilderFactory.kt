package com.anaplan.engineering.azuki.rightofway.adapter.vdm.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.rightofway.adapter.api.freshNames
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.VdmGenerationException
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircraft
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircrafts
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.VdmDeclaration
import com.anaplan.engineering.azuki.vdm.toVdmName

class AirspaceDeclarationBuilderFactory : VdmDeclarationBuilderFactory<AirspaceDeclaration> {

    override val declarationClass = AirspaceDeclaration::class.java
    override fun create(declaration: AirspaceDeclaration): VdmDeclarationBuilder<AirspaceDeclaration> =
        AirspaceDeclarationBuilder(declaration)

    private class AirspaceDeclarationBuilder(declaration: AirspaceDeclaration) :
        VdmDeclarationBuilder<AirspaceDeclaration>(declaration) {

        override fun imports(builder: DefaultModuleBuilder) =
            setOf(
                RightOfWayRulesModule.Airspace.import,
                RightOfWayRulesModule.Aircraft.import,
            )

        override fun declarations(builder: DefaultModuleBuilder, container: Declaration?): List<VdmDeclaration> {
            val airspaceNameGetter = builder.getters[declaration.name]
                ?: throw VdmGenerationException("Missing getter for model ${declaration.name}")
            return listOf(VdmDeclaration(declaration.vdmName(),
                RightOfWayRulesModule.Airspace,
                "mk_${RightOfWayRulesModule.Airspace}(${toVdmAircrafts(declaration.aircrafts)}, ${declaration.delta_o}, ${declaration.delta_c}), ${declaration.Theta_h}, ${declaration.opened})")) +
                declaration.aircrafts.map { (name, aircraft) ->
                    VdmDeclaration(toVdmName("${declaration.name}_${name}"),
                        RightOfWayRulesModule.Aircraft, toVdmAircraft(aircraft)) }
        }
    }
}

