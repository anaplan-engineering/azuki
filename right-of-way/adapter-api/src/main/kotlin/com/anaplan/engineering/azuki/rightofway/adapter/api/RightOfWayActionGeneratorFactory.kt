package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory

enum class Quadrant { FRONT_RIGHT, FRONT_LEFT, BACK_LEFT, BACK_RIGHT }

interface RightOfWayActionGeneratorFactory : ActionGeneratorFactory {

    /**
     * Generates a valid aircraft and assigns it to the given name.
     * An aircraft must not already exist with this name.
     */
    fun generateAircraft(airCraftName: String): ActionGenerator

    /**
     * Generates a new valid 3-D space position of where aircraft can be.
     * An aircraft must already exist with this name.
     */
    fun generatePosition(aircraftName: String): ActionGenerator

    /**
     * Generates a sequence of valid quadrant moves for the given aircraft.
     */
    fun generateQuadrant(aircraftName: String, quadrant: Quadrant): ActionGenerator

    fun generateMoves(aircraftName: String, numMoves: Int): ActionGenerator
}
