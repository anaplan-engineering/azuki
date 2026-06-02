package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory

/**
 * What can the model declare
 */
class RightOfWayGiven(private val actionFactory: RightOfWayActionFactory): Given<RightOfWayActionFactory> {

    private val actionList = mutableListOf<Action>()

    fun thereIsAnAirspace(airSpaceName: String) {
        actionList.add(actionFactory.airspace.start(airSpaceName))
    }

    fun thereIsAnAirspaceWithAircraft(airSpaceName: String, numberOfAircraft: UInt) {
        //actionList.add(actionFactory.aircrafts.create())
    }

//    fun thereIsAnAircraft(aircraftName: String) {
//        //actionList.add(actionFactory.aircraft.create(aircraftName))
//    }

//    fun thereIsAPlayOrder(orderName: String, vararg players: String) {
//        actionList.add(actionFactory.playOrder.create(orderName, players.toList()))
//    }
//
//    fun thereIsANewGame(gameName: String, orderName: String) {
//        actionList.add(actionFactory.game.start(gameName, orderName))
//    }
//
//    fun thereIsANewGameWithPlayers(gameName: String, vararg players: String) {
//        val orderName = "${gameName}_ORDER"
//        thereIsAPlayOrder(orderName, *players)
//        thereIsANewGame(gameName, orderName)
//    }
//
//    fun thereIsANewGame(gameName: String) {
//        thereIsANewGameWithPlayers(gameName, "X", "O")
//    }
//
//    fun thereIsAGame(gameName: String, orderName: String, boardData: String) {
//        thereIsANewGame(gameName, orderName)
//        RightOfWayBoardAscii.parse(boardData).forEach { (position, playerName) ->
//            actionList.add(actionFactory.game.move(gameName, playerName, position))
//        }
//    }
//
//    fun thereIsAGame(gameName: String, boardData: String) {
//        thereIsANewGame(gameName)
//        RightOfWayBoardAscii.parse(boardData).forEach { (position, playerName) ->
//            actionList.add(actionFactory.game.move(gameName, playerName, position))
//        }
//    }

    override fun actions(): List<Action> = actionList

}
