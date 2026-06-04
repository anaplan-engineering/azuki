package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

interface RightOfWayCheckFactory : CheckFactory {
    val airspace: AirspaceCheckFactory
    val aircraft: AircraftCheckFactory
}

interface AirspaceCheckFactory {
    fun hasRightOfWay(airspaceName: String, aircraft1: String, aircraft2: String): Check

    fun isConverging(airspaceName: String, aircraft1: String, aircraft2: String): Check
    fun isOvertaking(airspaceName: String, aircraft1: String, aircraft2: String): Check

    fun isGoingToCross(airspaceName: String, aircraft1: String, aircraft2: String): Check
    fun hasCrossed(airspaceName: String, aircraft1: String, aircraft2: String): Check
    fun hasZeroCrossed(airspaceName: String, aircraft1: String, aircraft2: String): Check
    fun hasOneCrossed(airspaceName: String, aircraft1: String, aircraft2: String): Check
    fun hasBothCrossed(airspaceName: String, aircraft1: String, aircraft2: String): Check

    fun hasQuadrantConvergence(airspaceName: String, aircraft1: String, aircraft2: String): Check
    fun horizontalMissDistanceIs(airspaceName: String, aircraft1: String, aircraft2: String, hmd: Double): Check
    fun hasOrientation(airspaceName: String, aircraft1: String, aircraft2: String, same: Boolean): Check

    fun timeToClosestPointApproachIs(aircraft1: String, aircraft2: String, tcpa: Double): Check // tcpa: NReal, NNZReal
}

interface AircraftCheckFactory {

    //TODO should this be linked with the Kazuki Angle type?
    fun onTrack(aircraftName: String, angle: Double): Check

    fun onQuadrantRelativeTo(aircraftName: String, position: Position, quadrant: Quadrant): Check
}
