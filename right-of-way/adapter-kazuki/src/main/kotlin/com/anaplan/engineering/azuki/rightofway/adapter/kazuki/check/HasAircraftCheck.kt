
package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class HasAircraftCheck(private val airspaceName: String, private val aircraftName: String,
) : ReifiedBehavior, KazukiCheck {
    override val behavior = RightOfWayBehaviours.HasAircraft
    override fun check(env: ExecutionEnvironment) =
        env.hasAircraft(airspaceName, aircraftName)
}
