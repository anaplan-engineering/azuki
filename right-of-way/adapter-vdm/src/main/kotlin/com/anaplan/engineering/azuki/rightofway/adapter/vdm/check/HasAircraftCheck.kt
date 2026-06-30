package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class HasAircraftCheck(private val airspaceName: String, private val aircraftName: String) :
    ReifiedBehavior, DefaultVdmCheck
{
    override val behavior = RightOfWayBehaviours.HasAircraft

    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder {
        // TODO: refactor this into abstract class?
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val aircraftGetter = builder.getters[toVdmName("${airspaceName}_${aircraftName}")] ?: throw IllegalStateException("Missing getter for aircraft ${airspaceName}_${aircraftName}")

        //  In VDM aircraft identity is on its unique position rather than name
        //  For implementation, this was sorted through the use of maps on execution environment
        //  For kazuki, this was sorted via execution environment variables
        //  For VDM, this is injected via the AirspaceDeclarationBuilderFactory.declarations method
        return builder.extend(
            requiredImports = setOf(RightOfWayRulesModule.has_aircraft.import),
            testSteps = listOf(
                """
                (
                    dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                    dcl aircraft: ${RightOfWayRulesModule.Aircraft} := $aircraftGetter;
                    dcl expected: bool := true;
                    ${checkEquals(actual = "${RightOfWayRulesModule.has_aircraft}(airspace, aircraft)")}
                );
                """
            )
        )
    }
}
