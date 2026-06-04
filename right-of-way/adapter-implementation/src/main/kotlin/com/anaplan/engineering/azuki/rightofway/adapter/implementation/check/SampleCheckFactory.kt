package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.rightofway.adapter.api.*
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class SampleCheckFactory : RightOfWayCheckFactory {

//    override val player = SamplePlayerCheckFactory
//    override val game = SampleGameCheckFactory
    override val airspace: SampleAirspaceCheckFactory
    override val aircraft: SampleAircraftCheckFactory

    override fun systemValid() = SystemValidCheck()
}

object SampleAircraftCheckFactory : AircraftCheckFactory {

    override fun onTrack(aircraftName: String, angle: Double) = OnTrackCheck(aircraftName, angle)
    override fun onQuadrantRelativeTo(aircraftName: String, position: Position, quadrant: Quadrant) =
        OnQuadrantRelativeToCheck(aircraftName, position, quadrant)
}

object SampleAirspaceCheckFactory : AirspaceCheckFactory {

    override fun hasRightOfWay(airspaceName: String, aircraft1: String, aircraft2: String) =
        HasRightOfWayCheck(airspaceName, aircraft1, aircraft2)

    override fun isConverging(airspaceName: String, aircraft1: String, aircraft2: String) =
        IsConvergingCheck(airspaceName, aircraft1, aircraft2)

    override fun isOvertaking(airspaceName: String, aircraft1: String, aircraft2: String) =
        IsOvertakingCheck(airspaceName, aircraft1, aircraft2)

    override fun isGoingToCross(airspaceName: String, aircraft1: String, aircraft2: String) =
        IsGoingToCross(airspaceName, aircraft1, aircraft2)

    override fun hasCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        HasCrossed(airspaceName, aircraft1, aircraft2)

    override fun hasZeroCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        HasZeroCrossed(airspaceName, aircraft1, aircraft2)

    override fun hasOneCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        HasOneCrossed(airspaceName, aircraft1, aircraft2)

    override fun hasBothCrossed(airspaceName: String, aircraft1: String, aircraft2: String) =
        HasBothCrossed(airspaceName, aircraft1, aircraft2)

    override fun hasQuadrantConvergence(airspaceName: String, aircraft1: String, aircraft2: String) =
        HasQuadrantConversionCheck(airspaceName, aircraft1, aircraft2)

    override fun horizontalMissDistanceIs(airspaceName: String, aircraft1: String, aircraft2: String, hmd: Double) =
        HorizontalMissDistanceIs(aircraft1, aircraft2, hmd)

    override fun hasOrientation(airspaceName: String, aircraft1: String, aircraft2: String, same: Boolean) =
        HasOrientationCheck(airspaceName, aircraft1, aircraft2, same)

    override fun timeToClosestPointApproachIs(aircraft1: String, aircraft2: String, tcpa: Double) = // tcpa: NReal, NNZReal
        TimeToClosestPointApproachIs(aircraft1, aircraft2, tcpa)
}

interface SampleCheck : Check {
    fun check(env: ExecutionEnvironment): Boolean
}
