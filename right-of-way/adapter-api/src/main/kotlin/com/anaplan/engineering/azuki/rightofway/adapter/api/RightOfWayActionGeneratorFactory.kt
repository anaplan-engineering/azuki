package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory

interface RightOfWayActionGeneratorFactory : ActionGeneratorFactory {

    fun generateAirspace(airSpaceName: String, numberOfAircraft: UInt): ActionGenerator

    /**
     * Generates a valid aircraft and assigns it to the given name.
     * An aircraft must not already exist with this name.
     */
    fun generateAircraft(airspaceName: String): ActionGenerator

    /**
     * Generates a new valid 3-D space position of where aircraft can be.
     * An aircraft must already exist with this name.
     */
    fun generatePositions(aircraftName: String, number: UInt): ActionGenerator

    fun generateVelocities(aircraftName: String, number: UInt): ActionGenerator

}
