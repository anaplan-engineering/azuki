package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.CreatePlayOrderDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class CreatePlayOrderAction(
    orderName: String,
    players: List<String>
) : CreatePlayOrderDeclarableAction(orderName, players), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.playOrders[orderName] = players
    }
}
