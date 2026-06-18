package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Convergence
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class QuadrantCheck(private val airspaceName: String, private val aircraft0: String,
                    private val aircraft1: String, private val quadrant: Quadrant) :
    ReifiedBehavior, DefaultVdmCheck {

    override val behavior = RightOfWayBehaviours.OnQuadrant

    private fun quadrantImport() = when (quadrant) {
        Quadrant.FRONT_RIGHT -> RightOfWayRulesModule.Q1.import
        Quadrant.FRONT_LEFT  -> RightOfWayRulesModule.Q2.import
        Quadrant.BACK_LEFT   -> RightOfWayRulesModule.Q3.import
        Quadrant.BACK_RIGHT  -> RightOfWayRulesModule.Q4.import
    }

    private fun quadrantFcn() = when (quadrant) {
        Quadrant.FRONT_RIGHT -> RightOfWayRulesModule.Q1
        Quadrant.FRONT_LEFT  -> RightOfWayRulesModule.Q2
        Quadrant.BACK_LEFT   -> RightOfWayRulesModule.Q3
        Quadrant.BACK_RIGHT  -> RightOfWayRulesModule.Q4
    }

    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val aircraft0Getter = builder.getters[toVdmName("${airspaceName}_${aircraft0}")] ?: throw IllegalStateException("Missing getter for first aircraft ${airspaceName}_${aircraft0}")
        val aircraft1Getter = builder.getters[toVdmName("${airspaceName}_${aircraft1}")] ?: throw IllegalStateException("Missing getter for second aircraft ${airspaceName}_${aircraft1}")

        return builder.extend(
            requiredImports = setOf(quadrantImport()),
            // TODO LF how to "get" the aircraft from the airspace? i.e. a0 in airspace.aircrafts etc...
            testSteps = listOf(
                """
            (
                --dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                dcl a0: ${RightOfWayRulesModule.Aircraft} := $aircraft0Getter;
                dcl a1: ${RightOfWayRulesModule.Aircraft} := $aircraft1Getter;
                dcl expected: bool := true;
                ${checkEquals(actual = "${quadrantFcn()}(a0, a1)")}
            );
            """
            )
        )
    }
}
