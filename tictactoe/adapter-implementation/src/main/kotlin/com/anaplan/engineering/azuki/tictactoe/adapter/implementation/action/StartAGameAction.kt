package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.StartAGameDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.toPlayer
import com.anaplan.engineering.azuki.tictactoe.implementation.Game

class StartAGameAction(
    gameName: String,
    orderName: String
) : StartAGameDeclarableAction(gameName, orderName), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        val playOrder = env.playOrders[orderName]!!.map(::toPlayer)
        env.gameManager.add(gameName, env.gameManager.gameCreator.create(*playOrder.toTypedArray()))
    }
}
