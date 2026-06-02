package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory

class RightOfWayGenerate(private val actionGeneratorFactory: RightOfWayActionGeneratorFactory) :
    Generate<RightOfWayActionGeneratorFactory> {

    fun createAirspace(airSpaceName: String, numberOfAirCraft: UInt = 2u) =
        addGenerator { generateAirspace(airSpaceName, 2u) }
    fun createAircraft(aircraftName: String) = addGenerator { generateAircraft(aircraftName) }
    fun createPositions(aircraftName: String, number: UInt = 3u) = addGenerator { generatePositions(aircraftName, number) }
    fun createVelocitys(aircraftName: String, number: UInt = 3u) = addGenerator { generateVelocities(aircraftName, number) }

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList

    private fun addGenerator(via: RightOfWayActionGeneratorFactory.() -> ActionGenerator) {
        generatorList.add(actionGeneratorFactory.via())
    }
}
