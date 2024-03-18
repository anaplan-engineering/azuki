package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.action

import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.StartAGameDeclarableAction

class StartAGameAction(gameName: String, orderName: String) :
    StartAGameDeclarableAction(gameName, orderName), DafnyAction {

    override fun act(env: ExecutionEnvironment) {
        TODO("Not yet implemented")
    }
}
