package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.rightofway.adapter.api.MIN_AIRCRAFT
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.THETA_H
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

    // Use delegation with receiver to make definitions streamlined (i.e. remove need to have many list.add explicit calls
    private fun addAction(via: RightOfWayActionFactory.() -> Action) {
        actionList.add(actionFactory.via())
    }

    fun thereIsANewAirspace(airspaceName: String, delta_o: Double = DELTA_O,
                            delta_c: Double = DELTA_C, theta_h: Double = THETA_H, opened: Boolean = true) {
        require(airspaceName.isNotBlank()) { "Airspace name must not be blank" }
        addAction { actionFactory.airspace.start(airspaceName, delta_o, delta_c, theta_h, opened) }
    }

    fun thereIsAnAirspace(airspaceName: String, airspaceData: String, delta_o: Double = DELTA_O,
                          delta_c: Double = DELTA_C, theta_h: Double = THETA_H, opened: Boolean = true) {
        thereIsANewAirspace(airspaceName, delta_o, delta_c, theta_h, opened)
        require(airspaceData.isNotBlank()) { "Airspace data must not be blank" }
        RightOfWayAirspaceJSON.parse(airspaceData).forEach { (aircraftName, aircraft) ->
            addAction { actionFactory.airspace.addAircraft(airspaceName, aircraftName, aircraft) }
        }
    }

    fun thereIsAnAirspace(airspaceName: String, delta_o: Double = DELTA_O,
                          delta_c: Double = DELTA_C, theta_h: Double = THETA_H, opened: Boolean = true,
                          init: AircraftBlock.() -> Unit) {
        thereIsANewAirspace(airspaceName, delta_o, delta_c, theta_h, opened)
        val aircraftBlock = AircraftBlock(actionFactory, airspaceName)
        aircraftBlock.init()
        actionList.addAll(aircraftBlock.actions())
    }

    //TODO needs better positioning spec
//    fun thereIsANewAirspaceWithAircraft(airspaceName: String, delta_o: Double = DELTA_O,
//                                        delta_c: Double = DELTA_C, theta_h: Double = THETA_H,
//                                        opened: Boolean = false, numberOfAircraft: UInt = MIN_AIRCRAFT) {
//        require(numberOfAircraft > 0U) { "Number of aircraft must be strictly-positive (> 0)"}
//        thereIsANewAirspace(airspaceName, delta_o, delta_c, theta_h, opened)
//        // get as many fresh aircraft information as requested and add them to airspace
//        freshNames().zip(spiralPositionsSequence().zip(spiralVelocitiesSequence())).map {
//            // zipped result is Sequence<String, Pair<Position, Velocity>>
//            (name, zipped) -> name to zipped.toAircraft() }.take(numberOfAircraft.toInt()).forEach {
//                (aircraftName, aircraft) ->
//                    addAction { actionFactory.airspace.addAircraft(airspaceName, aircraftName, aircraft) }
//                }
//    }

    override fun actions(): List<Action> = actionList
}

@ScenarioDsl
class AircraftBlock(private val actionFactory: RightOfWayActionFactory, val airspaceName: String) : Given<RightOfWayActionFactory> {

    private val actionList = mutableListOf<Action>()

    fun thereIsAnAircraft(aircraftName: String, aircraft: Aircraft) {
        actionList.add(actionFactory.airspace.addAircraft(airspaceName, aircraftName, aircraft))
    }

    override fun actions(): List<Action> = actionList
}
