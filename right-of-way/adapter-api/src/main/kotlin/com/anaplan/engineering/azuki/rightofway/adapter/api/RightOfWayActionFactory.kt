package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface RightOfWayActionFactory : ActionFactory {
    val aircraft: AircraftActionFactory
    val position: PositionActionFactory
}

interface PositionActionFactory {
    fun create(posName: String, aircraftName: String): Action
}

interface AircraftActionFactory {
    fun start(aircraftName: String): Action
    fun save(aircraftName: String): Action
    fun close(aircraftName: String): Action
    fun load(aircraftName: String): Action
    fun move(aircraftName: String, position: Position): Action
}
