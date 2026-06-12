package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

// behaviours being modelled?
// for every behaviour, will have a declarable action?
object RightOfWayBehaviours {
    const val StartAirspace = 1
    const val NewAircraft = 2
    const val LoadAirspace = 3
    const val SaveAirspace = 21
    const val HasAircraft = 20
    const val RightOfWay = 4
    const val Convergence = 5
    const val Overtaking = 6
    const val Crossing = 7
    const val Crossed = 8
    const val ZeroCrossed = 9
    const val OneCrossed = 10
    const val BothCrossed = 11
    const val QuadrantConvergence = 12
    const val HMD = 13
    const val Direction = 14
    const val TCPA = 15
    const val OnTrack = 16
    const val OnQuadrant = 17
    const val ConvergeNotHeadon = 18
    const val ConvergeHeadon = 19
}

// Each behaviour will have a corresponding factory, be that for an action or a check
open class StartAirspaceBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.StartAirspace
}

open class CreateAircraftBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.NewAircraft
}

open class LoadAirspaceBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.LoadAirspace
}

//open class MoveAircraftBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.MoveAircraft
//}
//
//open class RightOfWayBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.RightOfWay
//}
//
//open class ConvergenceBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.Convergence
//}
//
//open class OvertakingBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.Overtaking
//}
//
//open class CrossingBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.Crossing
//}
//
//open class CrossedBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.Crossed
//}
//open class ZeroCrossedBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.ZeroCrossed
//}
//
//open class OneCrossedBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.OneCrossed
//}
//
//open class BothCrossedBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.BothCrossed
//}
//
//open class QuadrantConvergenceBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.QuadrantConvergence
//}
//
//open class HMDBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.HMD
//}
//
//open class OrientationBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.Orientation
//}
//
//open class TCPABehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.TCPA
//}
//
//open class OnTrackBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.OnTrack
//}
//
//open class OnQuadrantBehaviour : ReifiedBehavior {
//    override val behavior = RightOfWayBehaviours.OnQuadrant
//}
