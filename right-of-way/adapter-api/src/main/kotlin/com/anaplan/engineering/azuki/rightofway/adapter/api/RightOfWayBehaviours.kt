package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

// behaviours being modelled?
// for every behaviour, will have a declarable action?
object RightOfWayBehaviours {
    const val NewAirspace = 1
    const val NewAircraft = 2
    const val MoveAircraft = 3
    const val CheckRightOfWay = 4
}

open class NewAirspaceBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.NewAirspace
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
