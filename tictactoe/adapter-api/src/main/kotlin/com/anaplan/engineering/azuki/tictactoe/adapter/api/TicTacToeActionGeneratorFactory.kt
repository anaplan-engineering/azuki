package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory

interface TicTacToeActionGeneratorFactory : ActionGeneratorFactory {

    /**
     * Generates a sequence of valid moves.
     */
    fun generateMoves(gameName: String, moveCountRange: IntRange): ActionGenerator
}
