package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

// behaviours being modelled?
object RightOfWayBehaviours {
    const val NewAircraft = 1
    const val MoveAircraft = 2
    const val CheckRightOfWay = 3
}

open class NewAircraftBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.NewAircraft
}

open class MoveAircraftBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.MoveAircraft
}

open class CheckRightOfWayBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.CheckRightOfWay
}
