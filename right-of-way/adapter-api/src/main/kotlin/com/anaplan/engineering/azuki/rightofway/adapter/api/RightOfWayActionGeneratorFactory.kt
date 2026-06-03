package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory

interface RightOfWayActionGeneratorFactory : ActionGeneratorFactory {

    fun generateAirspace(airspaceName: String): ActionGenerator

    /**
     * Generates a valid aircraft and assigns it to the given name.
     * An aircraft must not already exist with this name.
     */
    fun generateAircraft(airspaceName: String, numberOfAircraft: Int = 2): ActionGenerator

}
