package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.action

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.StartAGameDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.ExecutionEnvironment

class StartAGameAction(gameName: String, orderName: String) :
    StartAGameDeclarableAction(gameName, orderName), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        TODO("Not yet implemented")
    }


}
