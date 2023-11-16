package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.action

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.CreatePlayOrderDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.StartAGameDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment

class CreatePlayOrderAction(gameName: String, players: List<String>) :
    CreatePlayOrderDeclarableAction(gameName, players), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        TODO("Not yet implemented")
    }


}
