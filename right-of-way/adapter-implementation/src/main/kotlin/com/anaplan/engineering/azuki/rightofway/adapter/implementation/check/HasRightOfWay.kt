
package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviour
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.toPlayer

class HasRightOfWayCheck(
    private val airSpaceName: String,
    private val playerName: String,
) : RightOfWayBehaviour(), SampleCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        return env.withAirspace(airspaceName) {

            hasLost(toPlayer(playerName))
        }
    }
}
