
package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class AircraftCountCheck(private val airspaceName: String, private val expectedCount: ULong, private val expectedOpened: Boolean
) : ReifiedBehavior, DefaultVdmCheck {
    override val behavior = RightOfWayBehaviours.AircraftCount

    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")

        return builder.extend(
            // TODO LF how to "get" the aircraft from the airspace? i.e. a0 in airspace.aircrafts etc...
            testSteps = listOf(
                """
            (
                dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                dcl expected: bool := true;
                ${checkEquals(actual = "(airspace.open = $expectedOpened and card airspace.aircrafts = $expectedCount)")}
            );
            """
            )
        )
    }}
