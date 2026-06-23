package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.dsl.declaration.PurseDeclarations

class MondexGiven(private val actionFactory: MondexActionFactory<*>) : Given<MondexActionFactory<*>>,
    PurseDeclarations{

    private val actionList = mutableListOf<Action>(actionFactory.world.create(HashMap()))

    override fun actions(): List<Action> = actionList

    override fun thereIsAPurse(purseName: String, balance: Int, lost: Int) {
        require(balance >= 0 && lost >= 0)
        actionList.add(actionFactory.purse.create(purseName, Purse(balance.toULong(), lost.toULong())))
    }

}
