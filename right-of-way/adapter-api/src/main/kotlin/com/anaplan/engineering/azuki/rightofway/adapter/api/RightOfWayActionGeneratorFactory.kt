package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory

interface RightOfWayActionGeneratorFactory : ActionGeneratorFactory {

    fun generateAirspace(airspaceName: String, delta_o: Double = DELTA_O, delta_c: Double = DELTA_C, theta_h: Double = THETA_H, opened: Boolean = true): ActionGenerator

    fun generateAirspaceWithAircraft(airspaceName: String, numberOfAircraft: UInt = MIN_AIRCRAFT): ActionGenerator

}
