package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.action

import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.StartAGameDeclarableAction
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.DefaultVdmAction

class StartAGameAction(gameName: String, orderName: String) :
    StartAGameDeclarableAction(gameName, orderName), DefaultVdmAction {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        TODO("Not yet implemented")
    }

}
