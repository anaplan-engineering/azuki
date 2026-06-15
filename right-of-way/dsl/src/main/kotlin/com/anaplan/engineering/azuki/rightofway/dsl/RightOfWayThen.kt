package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory

class RightOfWayThen(private val checkFactory: RightOfWayCheckFactory): Then<RightOfWayCheckFactory> {

    private val checkList = mutableListOf<Check>()

    override fun checks() = checkList

    fun everythingIsOkay() {
        checkList.add(checkFactory.systemValid())
        checkFactory.systemValid()
    }

    fun hasAircraft(airspaceName: String, aircraftName: String) =
        checkFactory.airspace.hasAircraft(airspaceName, aircraftName)

    fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.hasRightOfWay(airspaceName, aircraft0, aircraft1)

    fun isConverging(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.isConverging(airspaceName, aircraft0, aircraft1)

    fun isOvertaking(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.isOvertaking(airspaceName, aircraft0, aircraft1)

    fun isHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.isHeadOn(airspaceName, aircraft0, aircraft1)

    fun isNotHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.isNotHeadOn(airspaceName, aircraft0, aircraft1)

    fun isGoingToCross(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.isGoingToCross(airspaceName, aircraft0, aircraft1)

    fun hasCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.hasCrossed(airspaceName, aircraft0, aircraft1)

    fun hasZeroCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.hasZeroCrossed(airspaceName, aircraft0, aircraft1)

    fun hasOneCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.hasOneCrossed(airspaceName, aircraft0, aircraft1)

    fun hasBothCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.hasBothCrossed(airspaceName, aircraft0, aircraft1)

    fun hasQuadrantConvergence(airspaceName: String, aircraft0: String, aircraft1: String) =
        checkFactory.airspace.hasQuadrantConvergence(airspaceName, aircraft0, aircraft1)

    fun horizontalMissDistanceIs(airspaceName: String, aircraft0: String, aircraft1: String, hmd: Double) =
        checkFactory.airspace.horizontalMissDistanceIs(airspaceName, aircraft0, aircraft1, hmd)

    fun hasOrientation(airspaceName: String, aircraft0: String, aircraft1: String, same: Boolean) =
        checkFactory.airspace.hasOrientation(airspaceName, aircraft0, aircraft1, same)

    fun timeToClosestPointApproachIs(airspaceName: String, aircraft0: String, aircraft1: String, tcpa: Double) =
        checkFactory.airspace.timeToClosestPointApproachIs(airspaceName, aircraft0, aircraft1, tcpa)

    fun onTrack(airspaceName: String, aircraftName: String, angle: Double) =
        checkFactory.airspace.onTrack(airspaceName, aircraftName, angle)

    fun onQuadrantRelativeTo(airspaceName: String, aircraft0: String, aircraft1: String, quadrant: Quadrant) =
        checkFactory.airspace.onQuadrantRelativeTo(airspaceName, aircraft0, aircraft1, quadrant)
}
