
package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class AircraftCountCheck(private val airspaceName: String, private val expectedCount: ULong, private val expectedOpened: Boolean
) : ReifiedBehavior, KazukiCheck {
    override val behavior = RightOfWayBehaviours.AircraftCount
    override fun check(env: ExecutionEnvironment) =
        env.withAirspace(airspaceName) {
            properties.aircraftCount == expectedCount &&
                opened == expectedOpened //&&
                //TODO LF: needs to check env too? Or just Kazuki properties?
                //env.aircraftCount(airspaceName) == expectedCount
        }
}
