package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory

class RightOfWayGenerate(private val actionGeneratorFactory: RightOfWayActionGeneratorFactory) :
    Generate<RightOfWayActionGeneratorFactory> {

    fun createAircraft(aircraftName: String) = addGenerator { generateAircraft(aircraftName) }

    /**
     * Creates a new game.
     *
     * A game must not already exist with this name.
     *
     * The new game will randomly choose one of the play orders already defined.  This means there needs to be at least
     * one use of `createPlayOrder`, or at least one play order previously set up in the `given` block.
     */
    fun createPosition(aircraftName: String) = addGenerator { generatePosition(aircraftName) }

    fun moveToQuadrant(aircraftName: String, quadrant: Quadrant = Quadrant.FRONT_LEFT) =
        addGenerator { generateQuadrant(aircraftName, quadrant) }

    /**
     * Adds a sequence of moves to the given game.
     */
    fun addMoves(aircraftName: String, numMoves: Int = 9) =
        addGenerator { generateMoves(aircraftName, numMoves) }

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList

    private fun addGenerator(via: RightOfWayActionGeneratorFactory.() -> ActionGenerator) {
        generatorList.add(actionGeneratorFactory.via())
    }
}
