package com.anaplan.engineering.azuki.rightofway.adapter.vdm.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircraft
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircrafts
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.vdm.VdmDeclaration
import com.anaplan.engineering.azuki.vdm.toVdmName

class AirspaceDeclarationBuilderFactory : VdmDeclarationBuilderFactory<AirspaceDeclaration> {

    override val declarationClass = AirspaceDeclaration::class.java
    override fun create(declaration: AirspaceDeclaration): VdmDeclarationBuilder<AirspaceDeclaration> =
        AirspaceDeclarationBuilder(declaration)

    private class AirspaceDeclarationBuilder(declaration: AirspaceDeclaration) :
        VdmDeclarationBuilder<AirspaceDeclaration>(declaration) {

        override fun nestedGetters(): Map<String, String> =
            declaration.aircrafts.keys.associate { name ->
                val vdmName = toVdmName("${declaration.name}_$name")
                vdmName to vdmName
            }

        override fun declarations(builder: RightOfWayModuleBuilder, container: Declaration?): List<VdmDeclaration> =
            listOf(
                VdmDeclaration(
                    declaration.vdmName(),
                    RightOfWayRulesModule.Airspace,
                    "mk_${RightOfWayRulesModule.Airspace}(${toVdmAircrafts(declaration.aircrafts)}, ${declaration.delta_o}, ${declaration.delta_c}, ${declaration.Theta_h}, ${declaration.opened})",
                ),
            ) + declaration.aircrafts.map { (name, aircraft) ->
                VdmDeclaration(
                    toVdmName("${declaration.name}_${name}"),
                    RightOfWayRulesModule.Aircraft,
                    toVdmAircraft(aircraft),
                )
            }
    }
}

