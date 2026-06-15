package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory

interface RightOfWayActionGeneratorFactory : ActionGeneratorFactory {

    fun generateAirspace(airspaceName: String): ActionGenerator

    fun generateAirspaceWithAircraft(airspaceName: String, numberOfAircraft: UInt = MIN_AIRCRAFT): ActionGenerator

}
