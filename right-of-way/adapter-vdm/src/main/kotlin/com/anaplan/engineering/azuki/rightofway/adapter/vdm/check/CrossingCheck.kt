package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Convergence
import com.anaplan.engineering.azuki.rightofway.adapter.api.Crossing
import com.anaplan.engineering.azuki.rightofway.adapter.api.toRightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class CrossingCheck(private val airspaceName: String, private val aircraft0: String,
                    private val aircraft1: String, private val crossing: Crossing) :
    ReifiedBehavior, DefaultVdmCheck {

    override val behavior = crossing.toRightOfWayBehaviours()

    private fun crossingImport() = when (crossing) {
        Crossing.Crossing -> RightOfWayRulesModule.going_to_cross.import
        Crossing.Crossed -> RightOfWayRulesModule.crossed.import
        Crossing.BothCrossed -> RightOfWayRulesModule.both_crossed.import
        Crossing.ZeroCrossed -> RightOfWayRulesModule.zero_crossed.import
        Crossing.OneCrossed -> RightOfWayRulesModule.one_crossed.import
    }

    private fun crossingFcn() = when (crossing) {
        Crossing.Crossing -> RightOfWayRulesModule.going_to_cross
        Crossing.Crossed -> RightOfWayRulesModule.crossed
        Crossing.BothCrossed -> RightOfWayRulesModule.both_crossed
        Crossing.ZeroCrossed -> RightOfWayRulesModule.zero_crossed
        Crossing.OneCrossed -> RightOfWayRulesModule.one_crossed
    }

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val aircraft0Getter = builder.getters[toVdmName("${airspaceName}_${aircraft0}")] ?: throw IllegalStateException("Missing getter for first aircraft ${airspaceName}_${aircraft0}")
        val aircraft1Getter = builder.getters[toVdmName("${airspaceName}_${aircraft1}")] ?: throw IllegalStateException("Missing getter for second aircraft ${airspaceName}_${aircraft1}")

        return builder.extend(
            requiredImports = setOf(
                RightOfWayRulesModule.Airspace.import,
                RightOfWayRulesModule.Aircraft.import,
                crossingImport(),
            ),
            // TODO LF how to "get" the aircraft from the airspace? i.e. a0 in airspace.aircrafts etc...
            testSteps = listOf(
                """
            (
                --dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                dcl a0: ${RightOfWayRulesModule.Aircraft} := $aircraft0Getter;
                dcl a1: ${RightOfWayRulesModule.Aircraft} := $aircraft1Getter;
                dcl expected: bool := true;
                ${checkEquals(actual = "${crossingFcn()}(a0, a1)")}
            );
            """
            )
        )
    }
}
