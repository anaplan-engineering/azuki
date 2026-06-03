package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory

class RightOfWayGenerate(private val actionGeneratorFactory: RightOfWayActionGeneratorFactory) :
    Generate<RightOfWayActionGeneratorFactory> {

    fun createAirspace(airspaceName: String) =
        addGenerator { generateAirspace(airspaceName) }
    fun createAircraft(aircraftName: String, numberOfAirCraft: UInt = 2u) =
        addGenerator { generateAircraft(aircraftName, numberOfAirCraft) }

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList

    private fun addGenerator(via: RightOfWayActionGeneratorFactory.() -> ActionGenerator) {
        generatorList.add(actionGeneratorFactory.via())
    }
}
