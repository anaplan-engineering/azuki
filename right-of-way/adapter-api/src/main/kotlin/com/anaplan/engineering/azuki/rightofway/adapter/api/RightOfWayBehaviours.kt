package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

// behaviours being modelled?
object RightOfWayBehaviours {
    const val NewGame = 1
    const val PlayerMoveCount = 2
    const val PlaceToken = 3
    const val CreatePlayOrder = 4
    const val GetPlayOrder = 5
    const val GameEnd = 6
    const val PlayerWon = 7
    const val PlayerLost = 8
    const val GameDrawn = 8
}

open class StartAGameBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.NewGame
}

open class PlayerMoveCountBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.PlayerMoveCount
}

open class PlaceATokenBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.PlaceToken
}

open class CreatePlayOrderBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.CreatePlayOrder
}

open class GetPlayOrderBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.GetPlayOrder
}
open class HasWonBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.PlayerWon
}
open class HasLostBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.PlayerLost
}
open class IsCompleteBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.GameEnd}

open class IsDrawnBehaviour : ReifiedBehavior {
    override val behavior = RightOfWayBehaviours.GameDrawn}
