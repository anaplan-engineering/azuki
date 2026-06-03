package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

// behaviours being modelled?
// for every behaviour, will have a declarable action?
object RightOfWayBehaviours {
    const val StartAirspace = 1
    const val NewAircraft = 2
    const val MoveAircraft = 3
    const val CheckRightOfWay = 4
}

open class StartAirspaceBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.StartAirspace
}

open class CreateAircraftBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.NewAircraft
}

open class MoveAircraftBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.MoveAircraft
}

open class CheckRightOfWayBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.CheckRightOfWay
}
