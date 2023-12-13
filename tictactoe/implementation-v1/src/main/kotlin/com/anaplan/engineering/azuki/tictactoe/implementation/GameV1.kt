package com.anaplan.engineering.azuki.tictactoe.implementation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GameV1 internal constructor(
    state: GameState
) : Game(state) {

    constructor(vararg player: Player, prepopulated: Map<Pair<Int, Int>, Token> = emptyMap()) :
        this(
            GameState(
                (0 until Size).map { r -> (0 until Size).map { c -> prepopulated[c to r] }.toMutableList() }
                    .toMutableList(),
                player.toList()
            )
        )

    companion object {
        const val Size = 3
    }

    override val log: Logger = LoggerFactory.getLogger(GameV1::class.java)

}

class GameV1Creator : GameCreator {

    override fun create(state: GameState) = GameV1(state)

    override fun create(vararg player: Player, prepopulated: Map<Pair<Int, Int>, Token>) = GameV1(*player, prepopulated = prepopulated)

}
