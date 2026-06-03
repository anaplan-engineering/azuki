package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface RightOfWayActionFactory : ActionFactory {
    val airspace: AirspaceActionFactory
//    val aircrafts: AircraftActionFactory
//    val position: RVectorActionFactory//PositionActionFactory
//    val velocity: RVectorActionFactory//VelocityActionFactory
}

interface AirspaceActionFactory {
    fun start(airspaceName: String): Action
    fun save(airspaceName: String): Action
    fun close(airspaceName: String): Action
    fun load(airspaceName: String): Action
    fun addAircraft(airspaceName: String, aircraftName: String): Action
}

//interface AircraftActionFactory {
//    fun create(aircraftName: String, position: Position, velocity: Velocity): Action
//    fun placeIn(aircraftName: String, airspaceName: String): Action
//    //fun moveTo(aircraftName: String, quadrant: Quadrant): Action
//}

////TODO should this be linked with the Kazuki types?
//interface RVectorActionFactory /*<R: RVector>*/ {
//    fun create(x: Double, y: Double): Action
//    //fun moveTo(aircraftName: String, vector: Pair<Double, Double>): Action
//}

//interface PositionActionFactory {
//    //TODO should this be linked with the Kazuki types?
//    fun create(x: Double, y: Double): Action
//    fun moveTo(aircraftName: String, position: Position): Action
//}
//
//interface VelocityActionFactory {
//    //TODO should this be linked with the Kazuki types?
//    fun create(aircraftName: String, x: Double, y: Double): Action
//    fun moveBy(aircraftName: String, velocity: Velocity): Action
//}


