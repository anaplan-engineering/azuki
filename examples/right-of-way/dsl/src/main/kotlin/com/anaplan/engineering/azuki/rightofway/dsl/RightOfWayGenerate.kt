package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.MIN_AIRCRAFT
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory

class RightOfWayGenerate(private val actionGeneratorFactory: RightOfWayActionGeneratorFactory) :
    Generate<RightOfWayActionGeneratorFactory> {

    fun generateAirspace(airspaceName: String) =
        addGenerator { generateAirspace(airspaceName) }
    fun generateAirspaceWithAircraft(aircraftName: String, numberOfAirCraft: UInt = MIN_AIRCRAFT) =
        addGenerator { generateAirspaceWithAircraft(aircraftName, numberOfAirCraft) }

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList

    private fun addGenerator(via: RightOfWayActionGeneratorFactory.() -> ActionGenerator) {
        generatorList.add(actionGeneratorFactory.via())
    }
}
