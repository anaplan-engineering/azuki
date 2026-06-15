package com.anaplan.engineering.azuki.rightofway.adapter.vdm.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircraft
import com.anaplan.engineering.azuki.vdm.DefaultVdmAction
import com.anaplan.engineering.azuki.vdm.EmptySystemContext
import com.anaplan.engineering.azuki.vdm.ModuleBuilder

class StartAirspaceAction(airspaceName: String, opened: Boolean) :
    StartAirspaceDeclarableAction(airspaceName, opened), DefaultVdmAction {

    override fun build(builder: ModuleBuilder<EmptySystemContext>): ModuleBuilder<EmptySystemContext> {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val airspaceSetter = builder.setters[airspaceName] ?: throw IllegalStateException("Missing setter for airspace $airspaceName")
        return builder.extend(
            // Import corresponding VDM needs for indirect call
            //        open_airspace: Airspace -> Airspace
            //        open_airspace(mk_Airspace(aircrafts, delta_o, delta_c, theta_h, -)) ==
            //            mk_Airspace(aircrafts, delta_o, delta_c, theta_h, true)
            requiredImports = setOf(
                RightOfWayRulesModule.Airspace.import,
                RightOfWayRulesModule.set_airspace.import,
            ),
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
