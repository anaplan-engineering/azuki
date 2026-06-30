package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory

interface TicTacToeActionGeneratorFactory : ActionGeneratorFactory {

    /**
     * Generates a valid play order and assigns it to the given name.
     * A play order must not already exist with this name.
     */
    fun generatePlayOrder(orderName: String): ActionGenerator

    /**
     * Generates a new, empty game using one of the play orders previously generated or defined.
     * A game must not already exist with this name.
     */
    fun generateGame(gameName: String): ActionGenerator


    /**
     * Generates a sequence of valid moves for the given game.
     */
    fun generateMoves(gameName: String, numMoves: Int): ActionGenerator
}
