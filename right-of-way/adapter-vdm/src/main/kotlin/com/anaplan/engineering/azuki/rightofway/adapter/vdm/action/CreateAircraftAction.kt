package com.anaplan.engineering.azuki.rightofway.adapter.vdm.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.toVdmAircraft
import com.anaplan.engineering.azuki.vdm.DefaultVdmAction
import com.anaplan.engineering.azuki.vdm.EmptySystemContext
import com.anaplan.engineering.azuki.vdm.ModuleBuilder

class CreateAircraftAction(airspaceName: String, aircraftName: String, aircraft: Aircraft) :
    CreateAircraftDeclarableAction(airspaceName, aircraftName, aircraft), DefaultVdmAction {

    override fun build(builder: ModuleBuilder<EmptySystemContext>): ModuleBuilder<EmptySystemContext> {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val airspaceSetter = builder.setters[airspaceName] ?: throw IllegalStateException("Missing setter for airspace $airspaceName")
        return builder.extend(
            // Import corresponding VDM needs for indirect call
            //          add_aircraft: Airspace * Aircraft -> Airspace
            //          add_aircraft(asp, a) == mu (asp, aircrafts |-> asp.aircrafts union {a})
            requiredImports = setOf(
                RightOfWayRulesModule.Airspace.import,
                RightOfWayRulesModule.Aircraft.import,
                RightOfWayRulesModule.String.import,
                RightOfWayRulesModule.add_aircraft.import,
            ),
            testSteps = listOf("""
                (
                    dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                    dcl aircraft: ${RightOfWayRulesModule.Aircrafts} := ${toVdmAircraft(aircraft)};
                    airspace := ${RightOfWayRulesModule.add_aircraft}(airspace, aircraft);
                    ${airspaceSetter("airspace")};
                );
            """)
        )
    }
}
