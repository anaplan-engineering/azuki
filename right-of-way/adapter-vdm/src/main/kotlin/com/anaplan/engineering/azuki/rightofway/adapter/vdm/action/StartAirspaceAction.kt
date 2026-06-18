package com.anaplan.engineering.azuki.rightofway.adapter.vdm.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircraft
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayVdmAction

class StartAirspaceAction(airspaceName: String, delta_o: Double, delta_c: Double, theta_h: Double, opened: Boolean) :
    StartAirspaceDeclarableAction(airspaceName, delta_o, delta_c, theta_h, opened), RightOfWayVdmAction {

    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val airspaceSetter = builder.setters[airspaceName] ?: throw IllegalStateException("Missing setter for airspace $airspaceName")
        return builder.extend(
            requiredImports = setOf(RightOfWayRulesModule.set_airspace.import),
            testSteps = listOf("""
                (
                    dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                    airspace := ${RightOfWayRulesModule.set_airspace}(airspace, $opened);
                    ${airspaceSetter("airspace")};
                );
            """)
        )
    }

}
