package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import PlayOrder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.StartAGameDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class StartAGameAction(
    gameName: String,
    orderName: String
) : StartAGameDeclarableAction(gameName, orderName), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        val playOrder = env.get(orderName) as PlayOrder
        env.add(gameName, Game.new(3, 3, *playOrder.toTypedArray()))
    }
}
