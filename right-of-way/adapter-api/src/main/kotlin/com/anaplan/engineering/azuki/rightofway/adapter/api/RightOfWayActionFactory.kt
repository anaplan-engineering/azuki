package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface RightOfWayActionFactory : ActionFactory {
    val airspace: AirSpaceActionFactory
    val aircrafts: AircraftActionFactory
    val position: PositionActionFactory
    val velocity: VelocityActionFactory
}

interface AirSpaceActionFactory {
    fun start(airSpaceName: String): Action
}

interface AircraftActionFactory {
    fun create(aircraftName: String, position: Position, velocity: Velocity): Action
    fun placeIn(aircraftName: String, airSpaceName: String): Action
    fun moveTo(aircraftName: String, quadrant: Quadrant): Action
}

interface PositionActionFactory {
    //TODO should this be linked with the Kazuki types?
    fun create(x: Double, y: Double): Action
    fun moveTo(aircraftName: String, position: Position): Action
}

interface VelocityActionFactory {
    //TODO should this be linked with the Kazuki types?
    fun create(aircraftName: String, x: Double, y: Double): Action
    fun moveBy(aircraftName: String, velocity: Velocity): Action
}


