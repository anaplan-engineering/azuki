package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.rightofway.adapter.api.AirspaceCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.Convergence
import com.anaplan.engineering.azuki.rightofway.adapter.api.Crossing
import com.anaplan.engineering.azuki.rightofway.adapter.api.Direction
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.toDirection
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class SampleCheckFactory : RightOfWayCheckFactory {
    override val airspace = SampleAirspaceCheckFactory
    override fun systemValid() = SystemValidCheck()
}

object SampleAirspaceCheckFactory : AirspaceCheckFactory {

    override fun aircraftCount(airspaceName: String, expectedCount: ULong, expectedOpen: Boolean) =
        AircraftCountCheck(airspaceName, expectedCount, expectedOpen)

    override fun hasAircraft(airspaceName: String, aircraftName: String) =
        HasAircraftCheck(airspaceName, aircraftName)

    override fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) =
        HasRightOfWayCheck(airspaceName, aircraft0, aircraft1)

    override fun isConverging(airspaceName: String, aircraft0: String, aircraft1: String) =
        ConvergingCheck(airspaceName, aircraft0, aircraft1)

    override fun isHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        HeadOnCheck(airspaceName, aircraft0, aircraft1)

    // Note that !isHeadOn(airspaceName, aircraft0, aircraft1) != isNotHeadOn(airspaceName, aircraft0, aircraft1)
    override fun isNotHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        ConvergingNotHeadOnCheck(airspaceName, aircraft0, aircraft1)

    override fun isOvertaking(airspaceName: String, aircraft0: String, aircraft1: String) =
        OvertakingCheck(airspaceName, aircraft0, aircraft1)

    override fun isGoingToCross(airspaceName: String, aircraft0: String, aircraft1: String) =
        CrossingCheck(airspaceName, aircraft0, aircraft1, Crossing.Crossing)

    override fun hasCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        CrossingCheck(airspaceName, aircraft0, aircraft1, Crossing.Crossed)

    override fun hasZeroCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        CrossingCheck(airspaceName, aircraft0, aircraft1, Crossing.ZeroCrossed)

    override fun hasOneCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        CrossingCheck(airspaceName, aircraft0, aircraft1, Crossing.OneCrossed)

    override fun hasBothCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        CrossingCheck(airspaceName, aircraft0, aircraft1, Crossing.BothCrossed)

    override fun hasQuadrantConvergence(airspaceName: String, aircraft0: String, aircraft1: String) =
        QuadrantConvergenceCheck(airspaceName, aircraft0, aircraft1, Convergence.Convergence)

    override fun horizontalMissDistanceIs(airspaceName: String, aircraft0: String, aircraft1: String, hmd: Double) =
        HorizontalMissDistanceCheck(airspaceName, aircraft0, aircraft1, hmd)

    override fun hasOrientation(airspaceName: String, aircraft0: String, aircraft1: String, same: Boolean) =
        DirectionCheck(airspaceName, aircraft0, aircraft1, same.toDirection())

    override fun timeToClosestPointApproachIs(airspaceName: String, aircraft0: String, aircraft1: String, tcpa: Double) = // tcpa: NReal, NNZReal
        TimeClosestPointApproachCheck(airspaceName, aircraft0, aircraft1, tcpa)

    override fun onTrack(airspaceName: String, aircraftName: String, angle: Double) =
        TrackCheck(airspaceName, aircraftName, angle)

    override fun onQuadrantRelativeTo(airspaceName: String, aircraft0: String, aircraft1: String, quadrant: Quadrant) =
        QuadrantCheck(airspaceName, aircraft0, aircraft1, quadrant)
}

interface SampleCheck : Check {
    fun check(env: ExecutionEnvironment): Boolean
}
