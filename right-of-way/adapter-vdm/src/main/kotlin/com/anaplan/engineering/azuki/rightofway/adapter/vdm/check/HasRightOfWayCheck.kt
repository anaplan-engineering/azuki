package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class HasRightOfWayCheck(
    private val airspaceName: String,
    private val withRightOfWay: String,
    private val givingWay: String,
) : ReifiedBehavior, DefaultVdmCheck {

    override val behavior = RightOfWayBehaviours.RightOfWay

    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val withRightOfWayGetter = builder.getters[toVdmName("${airspaceName}_$withRightOfWay")]
            ?: throw IllegalStateException("Missing getter for aircraft with right of way ${airspaceName}_$withRightOfWay")
        val givingWayGetter = builder.getters[toVdmName("${airspaceName}_$givingWay")]
            ?: throw IllegalStateException("Missing getter for aircraft giving way ${airspaceName}_$givingWay")

        return builder.extend(
            requiredImports = setOf(RightOfWayRulesModule.right_of_way.import),
            testSteps = listOf(
                """
                (
                    dcl airspace: ${RightOfWayRulesModule.Airspace} := $airspaceGetter;
                    dcl withRightOfWay: ${RightOfWayRulesModule.Aircraft} := $withRightOfWayGetter;
                    dcl givingWay: ${RightOfWayRulesModule.Aircraft} := $givingWayGetter;
                    dcl expected: bool := true;
                    ${checkEquals(actual = "${RightOfWayRulesModule.right_of_way}(withRightOfWay, givingWay)(airspace.delta_o, airspace.delta_c, airspace.Theta_h)")}
                );
                """
            )
        )
    }

}
