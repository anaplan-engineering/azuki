package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object TicTacToeBehaviours {
    const val NewGame = 1
    const val PlayerMoveCount = 2
    const val PlaceToken = 3
    const val CreatePlayOrder = 4
    const val GetPlayOrder = 5
    const val GameEnd = 6
}

open class StartAGameBehaviour : ReifiedBehavior {
    override val behavior = TicTacToeBehaviours.NewGame
}

open class PlayerMoveCountBehaviour : ReifiedBehavior {
    override val behavior = TicTacToeBehaviours.PlayerMoveCount
}

open class PlaceATokenBehaviour : ReifiedBehavior {
    override val behavior = TicTacToeBehaviours.PlaceToken
}

open class CreatePlayOrderBehaviour : ReifiedBehavior {
    override val behavior = TicTacToeBehaviours.CreatePlayOrder
}

open class GetPlayOrderBehaviour : ReifiedBehavior {
    override val behavior = TicTacToeBehaviours.GetPlayOrder
}
