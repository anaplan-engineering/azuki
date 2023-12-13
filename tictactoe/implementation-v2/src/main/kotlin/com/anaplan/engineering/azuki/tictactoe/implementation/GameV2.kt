package com.anaplan.engineering.azuki.tictactoe.implementation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GameV2 internal constructor(
    state: GameState
) : Game(state) {

    constructor(rows: Int, cols: Int, vararg player: Player, prepopulated: Map<Pair<Int, Int>, Token> = emptyMap()) :
        this(
            GameState(
                (0 until rows).map { r -> (0 until cols).map { c -> prepopulated[c to r] }.toMutableList() }
                    .toMutableList(),
                player.toList()
            )
        )

    override val log: Logger = LoggerFactory.getLogger(GameV2::class.java)
}

class GameV2Creator : GameCreator {

    override fun create(state: GameState) = GameV2(state)

    override fun create(vararg player: Player, prepopulated: Map<Pair<Int, Int>, Token>) = GameV2(3, 3, *player, prepopulated = prepopulated)

}
