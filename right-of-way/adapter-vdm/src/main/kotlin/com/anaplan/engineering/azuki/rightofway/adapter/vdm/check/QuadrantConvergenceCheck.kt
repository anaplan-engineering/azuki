package com.anaplan.engineering.azuki.rightofway.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Convergence
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.RightOfWayRulesModule
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmName

class QuadrantConvergenceCheck(private val airspaceName: String, private val aircraft0: String,
                                        private val aircraft1: String, private val convergence: Convergence) :
    ReifiedBehavior, DefaultVdmCheck {

    override val behavior = RightOfWayBehaviours.QuadrantConvergence

    private fun quadrantImport() = when (convergence) {
        Convergence.Convergence -> RightOfWayRulesModule.QC.import
        Convergence.Overtake -> RightOfWayRulesModule.QO.import
        Convergence.Divergence -> RightOfWayRulesModule.QD.import
    }

    private fun quadrantFcn() = when (convergence) {
        Convergence.Convergence -> RightOfWayRulesModule.QC
        Convergence.Overtake -> RightOfWayRulesModule.QO
        Convergence.Divergence -> RightOfWayRulesModule.QD
    }

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val airspaceGetter = builder.getters[airspaceName] ?: throw IllegalStateException("Missing getter for airspace $airspaceName")
        val aircraft0Getter = builder.getters[toVdmName("${airspaceName}_${aircraft0}")] ?: throw IllegalStateException("Missing getter for first aircraft ${airspaceName}_${aircraft0}")
        val aircraft1Getter = builder.getters[toVdmName("${airspaceName}_${aircraft1}")] ?: throw IllegalStateException("Missing getter for second aircraft ${airspaceName}_${aircraft1}")

        return builder.extend(
            requiredImports = setOf(
                RightOfWayRulesModule.Airspace.import,
                RightOfWayRulesModule.Aircraft.import,
                quadrantImport(),
            ),
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
