package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory

class TicTacToeGenerate(private val actionGeneratorFactory: TicTacToeActionGeneratorFactory) :
    Generate<TicTacToeActionGeneratorFactory> {

    /**
     * Creates a play order with the given name.
     * A play order must not already exist with this name.
     */
    fun createPlayOrder(orderName: String) = addGenerator { generatePlayOrder(orderName) }

    /**
     * Creates a new game.
     *
     * A game must not already exist with this name.
     *
     * The new game will randomly choose one of the play orders already defined.  This means there needs to be at least
     * one use of `createPlayOrder`, or at least one play order previously set up in the `given` block.
     */
    fun createNewGameFromExistingPlayOrder(gameName: String) = addGenerator { generateNewGame(gameName) }

    /**
     * Adds a sequence of moves to the given game.
     */
    fun addMoves(gameName: String, numMoves: Int = 9) =
        addGenerator { generateMoves(gameName, numMoves) }

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList

    private fun addGenerator(via: TicTacToeActionGeneratorFactory.() -> ActionGenerator) {
        generatorList.add(actionGeneratorFactory.via())
    }
}
