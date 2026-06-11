
package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class HasRightOfWayCheck(airspaceName: String, aircraft0: String, aircraft1: String,
) : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.RightOfWay) {
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        functions.right_of_way(aircraft0, aircraft1)(properties.delta_o, properties.delta_c, properties.Theta_h)
}
