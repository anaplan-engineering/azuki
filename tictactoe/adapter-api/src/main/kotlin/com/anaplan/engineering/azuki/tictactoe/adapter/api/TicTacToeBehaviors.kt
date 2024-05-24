package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object TicTacToeBehaviors {
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

open class StartAGameBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.NewGame
}

open class PlayerMoveCountBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.PlayerMoveCount
}

open class PlaceATokenBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.PlaceToken
}

open class CreatePlayOrderBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.CreatePlayOrder
}

open class GetPlayOrderBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.GetPlayOrder
}

open class HasWonBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.PlayerWon
}

open class HasLostBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.PlayerLost
}

open class IsCompleteBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.GameEnd
}

open class IsDrawnBehavior : ReifiedBehavior {
    override val behavior = TicTacToeBehaviors.GameDrawn
}
