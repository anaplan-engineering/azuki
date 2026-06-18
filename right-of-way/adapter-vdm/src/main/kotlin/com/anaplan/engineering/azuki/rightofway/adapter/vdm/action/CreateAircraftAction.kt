package com.anaplan.engineering.azuki.rightofway.adapter.vdm.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircraft
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayVdmAction

class CreateAircraftAction(airspaceName: String, aircraftName: String, aircraft: Aircraft) :
    CreateAircraftDeclarableAction(airspaceName, aircraftName, aircraft), RightOfWayVdmAction {

    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val airspaceSetter = builder.setters[airspaceName] ?: throw IllegalStateException("Missing setter for airspace $airspaceName")
        return builder.extend(
            requiredImports = setOf(
                RightOfWayRulesModule.String.import,
                RightOfWayRulesModule.add_aircraft.import,
            ),
            testSteps = listOf("""
                (
                    dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                    dcl aircraft: ${RightOfWayRulesModule.Aircraft} := ${toVdmAircraft(aircraft)};
                    airspace := ${RightOfWayRulesModule.add_aircraft}(airspace, aircraft);
                    ${airspaceSetter("airspace")};
                );
            """)
        )
    }
}
