package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.MIN_AIRCRAFT
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.freshNames
import com.anaplan.engineering.azuki.rightofway.adapter.api.spiralPositionsSequence
import com.anaplan.engineering.azuki.rightofway.adapter.api.spiralVelocitiesSequence
import com.anaplan.engineering.azuki.rightofway.adapter.api.toAircraft
import com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl.json.RightOfWayAirspaceJSON

/**
 * What can the model declare
 */
class RightOfWayGiven(private val actionFactory: RightOfWayActionFactory): Given<RightOfWayActionFactory> {

    private val actionList = mutableListOf<Action>()


    fun thereIsANewAirspace(airspaceName: String) {
        actionList.add(actionFactory.airspace.start(airspaceName))
    }

//    fun thereIsAnAircraft(aircraftName: String) {
//        actionList.add(actionFactory.airspace.addAircraft(aircraftName))
//    }

    fun thereIsAnAirspace(airspaceName: String, airspaceData: String) {
        thereIsANewAirspace(airspaceName)
        RightOfWayAirspaceJSON.parse(airspaceData).forEach { (name, aircraft) ->
            actionList.add(actionFactory.airspace.addAircraft(airspaceName, name, aircraft))
        }
    }

    fun thereIsAnAirspace(airspaceName: String, init: AircraftBlock.() -> Unit) {
        thereIsANewAirspace(airspaceName)
        val aircraftBlock = AircraftBlock(actionFactory, airspaceName)
        aircraftBlock.init()
        actionList.addAll(aircraftBlock.actions())
    }

    fun thereIsANewAirspaceWithAircraft(airspaceName: String, numberOfAircraft: UInt = MIN_AIRCRAFT) {
        require(numberOfAircraft > 0U) { "Number of aircraft must be strictly-positive (> 0)"}
        thereIsANewAirspace(airspaceName)
        // get as many fresh aircraft information as requested and add them to airspace
        freshNames().zip(spiralPositionsSequence().zip(spiralVelocitiesSequence())).map {
            // zipped result is Sequence<String, Pair<Position, Velocity>>
            (name, zipped) -> name to zipped.toAircraft() }.take(numberOfAircraft.toInt()).forEach {
                (aircraftName, aircraft) ->
                    actionList.add(actionFactory.airspace.addAircraft(airspaceName, aircraftName, aircraft))
                }
    }

    override fun actions(): List<Action> = actionList
}

class AircraftBlock(private val actionFactory: RightOfWayActionFactory, val airspaceName: String) : Given<RightOfWayActionFactory> {

    private val actionList = mutableListOf<Action>()

    fun thereIsAnAircraft(aircraftName: String, aircraft: Aircraft) {
        actionList.add(actionFactory.airspace.addAircraft(airspaceName, aircraftName, aircraft))
    }

    override fun actions(): List<Action> = actionList
}
