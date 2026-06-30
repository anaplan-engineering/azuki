package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory

class RightOfWayThen(private val checkFactory: RightOfWayCheckFactory): Then<RightOfWayCheckFactory> {

    private val checkList = mutableListOf<Check>()

    private fun addCheck(via: RightOfWayCheckFactory.() -> Check) {
        checkList.add(checkFactory.via())
    }

    override fun checks() = checkList

    fun everythingIsOkay() {
        checkList.add(checkFactory.systemValid())
        checkFactory.systemValid()
    }

    fun hasAircraft(airspaceName: String, aircraftName: String) =
        addCheck { checkFactory.airspace.hasAircraft(airspaceName, aircraftName) }

    fun aircraftCount(airspaceName: String, expectedCount: ULong, expectedOpen: Boolean) =
        addCheck { checkFactory.airspace.aircraftCount(airspaceName, expectedCount, expectedOpen) }

    fun hasRightOfWay(airspaceName: String, withRightOfWay: String, givingWay: String) =
        addCheck { checkFactory.airspace.hasRightOfWay(airspaceName, withRightOfWay, givingWay) }

    fun isConverging(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.isConverging(airspaceName, aircraft0, aircraft1) }

    fun isOvertaking(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.isOvertaking(airspaceName, aircraft0, aircraft1) }

    fun isHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.isHeadOn(airspaceName, aircraft0, aircraft1) }

    fun isNotHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.isNotHeadOn(airspaceName, aircraft0, aircraft1) }

    fun isGoingToCross(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.isGoingToCross(airspaceName, aircraft0, aircraft1) }

    fun hasCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.hasCrossed(airspaceName, aircraft0, aircraft1) }

    fun hasZeroCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.hasZeroCrossed(airspaceName, aircraft0, aircraft1) }

    fun hasOneCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.hasOneCrossed(airspaceName, aircraft0, aircraft1) }

    fun hasBothCrossed(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.hasBothCrossed(airspaceName, aircraft0, aircraft1) }

    fun hasQuadrantConvergence(airspaceName: String, aircraft0: String, aircraft1: String) =
        addCheck { checkFactory.airspace.hasQuadrantConvergence(airspaceName, aircraft0, aircraft1) }

    fun horizontalMissDistanceIs(airspaceName: String, aircraft0: String, aircraft1: String, hmd: Double) =
        addCheck { checkFactory.airspace.horizontalMissDistanceIs(airspaceName, aircraft0, aircraft1, hmd) }

    fun hasOrientation(airspaceName: String, aircraft0: String, aircraft1: String, same: Boolean) =
        addCheck { checkFactory.airspace.hasOrientation(airspaceName, aircraft0, aircraft1, same) }

    fun timeToClosestPointApproachIs(airspaceName: String, aircraft0: String, aircraft1: String, tcpa: Double) =
        addCheck { checkFactory.airspace.timeToClosestPointApproachIs(airspaceName, aircraft0, aircraft1, tcpa) }

    fun onTrack(airspaceName: String, aircraftName: String, angle: Double) =
        addCheck { checkFactory.airspace.onTrack(airspaceName, aircraftName, angle) }

    fun onQuadrantRelativeTo(airspaceName: String, aircraft0: String, aircraft1: String, quadrant: Quadrant) =
        addCheck { checkFactory.airspace.onQuadrantRelativeTo(airspaceName, aircraft0, aircraft1, quadrant) }
}
