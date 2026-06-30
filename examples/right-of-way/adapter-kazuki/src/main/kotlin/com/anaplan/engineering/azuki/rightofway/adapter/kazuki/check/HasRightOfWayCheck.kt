
package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.toKazuki

class HasRightOfWayCheck(
    private val airspaceName: String,
    private val withRightOfWay: String,
    private val givingWay: String,
) : ReifiedBehavior, KazukiCheck {

    override val behavior = RightOfWayBehaviours.RightOfWay

    override fun check(env: ExecutionEnvironment): Boolean =
        env.withAirspace(airspaceName) {
            val holder = env.aircraft(airspaceName, withRightOfWay).toKazuki()
            val giver = env.aircraft(airspaceName, givingWay).toKazuki()
            functions.right_of_way(holder, giver)(delta_o, delta_c, Theta_h)
        }
}
