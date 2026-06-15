package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Direction
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class DirectionCheck(private val airspaceName: String, private val aircraft0: String,
                     private val aircraft1: String, private val direction: Direction) :
    ReifiedBehavior, DefaultVdmCheck {

    override val behavior = RightOfWayBehaviours.Direction

    private fun directionImport() = when (direction) {
        Direction.Same -> RightOfWayRulesModule.same_orientation.import
        Direction.Opposite -> RightOfWayRulesModule.opposite_orientation.import
    }

    private fun directionFcn() = when (direction) {
        Direction.Same -> RightOfWayRulesModule.same_orientation
        Direction.Opposite -> RightOfWayRulesModule.opposite_orientation
    }

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val aircraft0Getter = builder.getters[toVdmName("${airspaceName}_${aircraft0}")] ?: throw IllegalStateException("Missing getter for first aircraft ${airspaceName}_${aircraft0}")
        val aircraft1Getter = builder.getters[toVdmName("${airspaceName}_${aircraft1}")] ?: throw IllegalStateException("Missing getter for second aircraft ${airspaceName}_${aircraft1}")

        return builder.extend(
            requiredImports = setOf(
                RightOfWayRulesModule.Airspace.import,
                RightOfWayRulesModule.Aircraft.import,
                directionImport(),
            ),
            // TODO LF how to "get" the aircraft from the airspace? i.e. a0 in airspace.aircrafts etc...
            testSteps = listOf(
                """
            (
                --dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                dcl a0: ${RightOfWayRulesModule.Aircraft} := $aircraft0Getter;
                dcl a1: ${RightOfWayRulesModule.Aircraft} := $aircraft1Getter;
                dcl expected: bool := true;
                ${checkEquals(actual = "${directionFcn()}(a0, a1)")}
            );
            """
            )
        )
    }
}
