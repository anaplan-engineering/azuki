package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.rightofway.adapter.api.AirspaceCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.implementation.Convergence
import com.anaplan.engineering.azuki.rightofway.implementation.Direction
import com.anaplan.engineering.azuki.rightofway.implementation.Quadrant

// TODO this looks ugly. Either change name, or how best to map these "api-level" x "impl-level" enums?
fun com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant.toImplQuadrant() = Quadrant.entries[ordinal]

class SampleCheckFactory : RightOfWayCheckFactory {
    override val airspace = SampleAirspaceCheckFactory
    override fun systemValid() = SystemValidCheck()
}

object SampleAirspaceCheckFactory : AirspaceCheckFactory {

    override fun hasRightOfWay(airspaceName: String, aircraft1: String, aircraft2: String) =
        HasRightOfWayCheck(airspaceName, aircraft1, aircraft2)

    override fun isConverging(airspaceName: String, aircraft1: String, aircraft2: String) =
        ConvergingCheck(airspaceName, aircraft1, aircraft2)

    override fun isOvertaking(airspaceName: String, aircraft1: String, aircraft2: String) =
        OvertakingCheck(airspaceName, aircraft1, aircraft2)

    override fun isGoingToCross(airspaceName: String, aircraft1: String, aircraft2: String) =
        CrossingCheck(airspaceName, aircraft1, aircraft2, Crossing.Crossing)

    override fun hasCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        CrossingCheck(airspaceName, aircraft1, aircraft2, Crossing.Crossed)

    override fun hasZeroCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        CrossingCheck(airspaceName, aircraft1, aircraft2, Crossing.ZeroCrossed)

    override fun hasOneCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        CrossingCheck(airspaceName, aircraft1, aircraft2, Crossing.OneCrossed)

    override fun hasBothCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        CrossingCheck(airspaceName, aircraft1, aircraft2, Crossing.BothCrossed)

    override fun hasQuadrantConvergence(airspaceName: String, aircraft1: String, aircraft2: String) =
        QuadrantConvergenceCheck(airspaceName, aircraft1, aircraft2, Convergence.Convergence)

    override fun horizontalMissDistanceIs(airspaceName: String, aircraft1: String, aircraft2: String, hmd: Double) =
        HorizontalMissDistanceCheck(airspaceName, aircraft1, aircraft2, hmd)

    override fun hasOrientation(airspaceName: String, aircraft1: String, aircraft2: String, same: Boolean) =
        DirectionCheck(airspaceName, aircraft1, aircraft2, if (same) Direction.Same else Direction.Opposite)

    override fun timeToClosestPointApproachIs(airspaceName: String, aircraft1: String, aircraft2: String, tcpa: Double) = // tcpa: NReal, NNZReal
        TimeClosestPointApproachCheck(airspaceName, aircraft1, aircraft2, tcpa)

    override fun onTrack(airspaceName: String, aircraftName: String, angle: Double) =
        TrackCheck(airspaceName, aircraftName, angle)

    override fun onQuadrantRelativeTo(airspaceName: String, aircraft1: String, aircraft2: String,
                                      quadrant: com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant) =
        QuadrantCheck(airspaceName, aircraft1, aircraft2, quadrant.toImplQuadrant())
}

interface SampleCheck : Check {
    fun check(env: ExecutionEnvironment): Boolean
}
