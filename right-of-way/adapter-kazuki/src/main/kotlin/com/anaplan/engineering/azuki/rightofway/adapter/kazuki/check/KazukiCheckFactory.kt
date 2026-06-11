package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.*
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment

class KazukiCheckFactory : RightOfWayCheckFactory {

    override val airspace = KazukiAirspaceCheckFactory

    override fun systemValid() = object : KazukiCheck {
        override val behavior = unsupportedBehavior
        override fun check(env: ExecutionEnvironment) = true
    }
}

object KazukiAirspaceCheckFactory : AirspaceCheckFactory {
    override fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) =
        HasRightOfWayCheck(airspaceName, aircraft0, aircraft1)

    override fun isConverging(airspaceName: String, aircraft0: String, aircraft1: String) =
        ConvergingCheck(airspaceName, aircraft0, aircraft1)

    override fun isOvertaking(airspaceName: String, aircraft0: String, aircraft1: String) =
        OvertakingCheck(airspaceName, aircraft0, aircraft1)

    override fun isGoingToCross(airspaceName: String, aircraft0: String, aircraft1: String) =
        CrossingCheck(airspaceName, aircraft0, aircraft1, Crossing.Crossing)

    override fun isHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        HeadOnCheck(airspaceName, aircraft0, aircraft1)

    override fun isNotHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        ConvergingNotHeadOnCheck(airspaceName, aircraft0, aircraft1)

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
        DirectionCheck(airspaceName, aircraft0, aircraft1, same)

    override fun timeToClosestPointApproachIs(airspaceName: String, aircraft0: String, aircraft1: String, tcpa: Double) =
        TimeClosestPointApproachCheck(airspaceName, aircraft0, aircraft1, tcpa)

    override fun onTrack(airspaceName: String, aircraftName: String, angle: Double) =
        TrackCheck(airspaceName, aircraftName, angle)

    override fun onQuadrantRelativeTo(airspaceName: String, aircraft0: String, aircraft1: String, quadrant: Quadrant) =
        QuadrantCheck(airspaceName, aircraft0, aircraft1, quadrant)
}

interface KazukiCheck : Check {

    fun check(env: ExecutionEnvironment): Boolean
}
