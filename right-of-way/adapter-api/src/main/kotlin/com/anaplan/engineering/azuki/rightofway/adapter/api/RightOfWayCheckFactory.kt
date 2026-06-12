package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

interface RightOfWayCheckFactory : CheckFactory {
    val airspace: AirspaceCheckFactory
}

interface AirspaceCheckFactory {
    fun hasAircraft(airspaceName: String, aircraftName: String): Check
    fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String): Check

    fun isConverging(airspaceName: String, aircraft0: String, aircraft1: String): Check
    fun isOvertaking(airspaceName: String, aircraft0: String, aircraft1: String): Check

    fun isHeadOn(airspaceName: String, aircraft0: String, aircraft1: String): Check
    fun isNotHeadOn(airspaceName: String, aircraft0: String, aircraft1: String): Check

    fun isGoingToCross(airspaceName: String, aircraft0: String, aircraft1: String): Check
    fun hasCrossed(airspaceName: String, aircraft0: String, aircraft1: String): Check
    fun hasZeroCrossed(airspaceName: String, aircraft0: String, aircraft1: String): Check
    fun hasOneCrossed(airspaceName: String, aircraft0: String, aircraft1: String): Check
    fun hasBothCrossed(airspaceName: String, aircraft0: String, aircraft1: String): Check

    fun hasQuadrantConvergence(airspaceName: String, aircraft0: String, aircraft1: String): Check
    fun horizontalMissDistanceIs(airspaceName: String, aircraft0: String, aircraft1: String, hmd: Double): Check
    fun hasOrientation(airspaceName: String, aircraft0: String, aircraft1: String, same: Boolean): Check
    fun timeToClosestPointApproachIs(airspaceName: String, aircraft0: String, aircraft1: String, tcpa: Double): Check // tcpa: NReal, NNZReal

    fun onTrack(airspaceName: String, aircraftName: String, angle: Double): Check
    fun onQuadrantRelativeTo(airspaceName: String, aircraft0: String, aircraft1: String, quadrant: Quadrant): Check
}
