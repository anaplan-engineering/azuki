package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class StartAirspaceAction(
    airspaceName: String
) : StartAirspaceDeclarableAction(airspaceName), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.withAirspace(airSpaceName) {
            println(this.aircraftIds)
            println(positions)
        }
        env.airspaceManager.add(airSpaceName, env.airspaceManager.airspaceCreator.create())
        TODO("Need a way to encode positions in execution environment")
//        val playOrder = env.playOrders[orderName]!!.map(::toPlayer)
//        env.gameManager.add(gameName, env.gameManager.gameCreator.create(*playOrder.toTypedArray()))
    }
}
