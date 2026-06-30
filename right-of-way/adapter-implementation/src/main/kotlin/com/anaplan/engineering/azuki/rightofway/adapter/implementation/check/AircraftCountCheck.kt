
package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class AircraftCountCheck(private val airspaceName: String, private val expectedCount: ULong, private val expectedOpened: Boolean
) : ReifiedBehavior, SampleCheck {
    override val behavior = RightOfWayBehaviours.AircraftCount
    override fun check(env: ExecutionEnvironment) =
        env.withAirspace(airspaceName) {
            size.toULong() == expectedCount &&
            open == expectedOpened
        }
}
