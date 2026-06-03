package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

interface RightOfWayCheckFactory : CheckFactory {
    val aircraft: AircraftCheckFactory
}

interface AirspaceCheckFactory {
    fun hasRightOfWay(airspaceName: String, airCraft1: String, airCraft2: String): Check

    fun isConverging(airspaceName: String, airCraft1: String, airCraft2: String): Check
    fun isOvertaking(airspaceName: String, airCraft1: String, airCraft2: String): Check

    fun isGoingToCross(airspaceName: String, airCraft1: String, airCraft2: String): Check
    fun hasCrossed(airspaceName: String, airCraft1: String, airCraft2: String): Check
    fun hasZeroCrossed(airspaceName: String, airCraft1: String, airCraft2: String): Check
    fun hasOneCrossed(airspaceName: String, airCraft1: String, airCraft2: String): Check
    fun hasBothCrossed(airspaceName: String, airCraft1: String, airCraft2: String): Check

    fun hasQuadrantConversion(airspaceName: String, airCraft1: String, airCraft2: String): Check
    fun horizontalMissDistanceIs(airCraft1: String, airCraft2: String, hmd: Double): Check
    fun hasOrientation(airspaceName: String, airCraft1: String, airCraft2: String, same: Boolean): Check

    fun timeToClosestPointApproachIs(airCraft1: String, airCraft2: String, tcpa: Double): Check // tcpa: NReal, NNZReal
}

interface AircraftCheckFactory {

    //TODO should this be linked with the Kazuki Angle type?
    fun onTrack(aircraftName: String, angle: Double): Check

    fun onQuadrantRelativeTo(aircraftName: String, position: Position, quadrant: Quadrant): Check
}

interface GameCheckFactory {

    fun hasPlayOrder(gameName: String, players: List<String>): Check
    fun hasToken(gameName: String, playerName: String, position: Position): Check
    fun hasSpace(gameName: String, position: Position): Check
    //fun hasState(gameName: String, moves: PositionMap): Check
    fun isComplete(gameName: String): Check
    fun isDraw(gameName: String): Check
}
