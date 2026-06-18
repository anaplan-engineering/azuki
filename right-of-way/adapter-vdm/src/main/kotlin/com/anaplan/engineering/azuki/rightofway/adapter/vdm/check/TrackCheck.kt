package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Convergence
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class TrackCheck(private val airspaceName: String, private val aircraft0: String,
                 private val angle: Double) :
    ReifiedBehavior, DefaultVdmCheck {

    override val behavior = RightOfWayBehaviours.OnTrack

    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val aircraft0Getter = builder.getters[toVdmName("${airspaceName}_${aircraft0}")] ?: throw IllegalStateException("Missing getter for aircraft ${airspaceName}_${aircraft0}")

        return builder.extend(
            requiredImports = setOf(
                RightOfWayRulesModule.Angle.import,
                RightOfWayRulesModule.track.import,
            ),
            // TODO LF how to "get" the aircraft from the airspace? i.e. a0 in airspace.aircrafts etc...
            testSteps = listOf(
                """
            (
                --dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                dcl a0: ${RightOfWayRulesModule.Aircraft} := $aircraft0Getter;
                dcl expected: ${RightOfWayRulesModule.Angle} := ${angle};
                ${checkEquals(actual = "${RightOfWayRulesModule.track}(a0)")}
            );
            """
            )
        )
    }
}
