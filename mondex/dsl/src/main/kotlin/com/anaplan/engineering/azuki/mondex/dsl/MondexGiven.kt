package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.Status
import com.anaplan.engineering.azuki.mondex.dsl.declaration.PurseDeclarations

class MondexGiven(private val actionFactory: MondexActionFactory<*>) : Given<MondexActionFactory<*>>,
    PurseDeclarations{

    private val actionList = mutableListOf<Action>(actionFactory.world.create(HashMap()))

    override fun actions(): List<Action> = actionList

    //LF @EK which purse is there? Abstract x Concrete etc?
    //    That's why I was saying if we go with the A/B/C worlds as different implementations, we will need some way
    //    of saying which implementation we are "declaring" in the DSL, if the DSL is shared between them.
    override fun thereIsAPurse(purseName: String, balance: Int, lost: Int) {
        //LF @EK if ULong, this is redundant
        require(balance >= 0 && lost >= 0)
        //LF @EK if you are sharing the DSLs how will deal with parameter difference between purses?
        //    //ConPurse(balance.toULong(), lost.toULong())???? NEed another one for it or something like a when below
        actionList.add(actionFactory.purse.create(purseName, AbPurse(balance.toULong(), lost.toULong())))
    }

    override fun thereIsAPurse(
        purseName: String,
        balance: ULong,
        exLog: Set<PayDetails>,
        nextSeqNo: ULong,
        pdAuth: PayDetails,
        status: Status
    ) {
        actionList.add(actionFactory.purse.create(purseName, ConPurse(balance, exLog, purseName, nextSeqNo, pdAuth, status)))
    }

}
