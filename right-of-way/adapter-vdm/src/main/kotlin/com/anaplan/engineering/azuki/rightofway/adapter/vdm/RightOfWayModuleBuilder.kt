package com.anaplan.engineering.azuki.rightofway.adapter.vdm

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.EmptySystemContext
import com.anaplan.engineering.azuki.vdm.VdmAction
import com.anaplan.engineering.azuki.vdm.VdmCheck
import com.anaplan.engineering.vdmanimation.api.Import
import com.anaplan.engineering.vdmanimation.api.SpecificationStructure

val rightOfWayRequiredImports: Set<Import> = setOf(
    RightOfWayRulesModule.Airspace.import,
    RightOfWayRulesModule.Aircraft.import,
    RightOfWayRulesModule.RVector.import,
)

typealias RightOfWayModuleBuilder = DefaultModuleBuilder

fun rightOfWayModuleBuilder(specification: SpecificationStructure): RightOfWayModuleBuilder =
    DefaultModuleBuilder(specification, EmptySystemContext, requiredImports = rightOfWayRequiredImports)

interface RightOfWayVdmAction : VdmAction<EmptySystemContext> {
    override fun build(builder: RightOfWayModuleBuilder): RightOfWayModuleBuilder
}

val toRightOfWayVdmAction: (Action) -> RightOfWayVdmAction = {
    @Suppress("UNCHECKED_CAST")
    it as? RightOfWayVdmAction ?: throw IllegalArgumentException("Invalid action: $it")
}
