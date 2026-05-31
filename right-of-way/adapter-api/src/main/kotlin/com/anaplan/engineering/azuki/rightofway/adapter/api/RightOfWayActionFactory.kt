package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface RightOfWayActionFactory : ActionFactory {
    val aircraft: AircraftActionFactory
    val position: PositionActionFactory
    val velocity: VelocityActionFactory
}

interface PositionActionFactory {
    //TODO should this be linked with the Kazuki types?
    fun create(aircraftName: String, x: Double, y: Double): Action
}

interface VelocityActionFactory {
    //TODO should this be linked with the Kazuki types?
    fun create(aircraftName: String, x: Double, y: Double): Action
}

interface AircraftActionFactory {
    fun start(aircraftName: String): Action
    fun save(aircraftName: String): Action
    fun move(aircraftName: String, position: Position, velocity: Velocity): Action
}
