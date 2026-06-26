package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.ParallelWhen
import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetails
import com.anaplan.engineering.azuki.mondex.dsl.action.WorldActions

class MondexWhen(private val actionFactory: MondexActionFactory<*>) :
    When<MondexActionFactory<*>>,
    ParallelWhen<MondexActionFactory<*>, MondexWhen>,
    WorldActions {

    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

    //LF @EK here you will need a way to separate what kind of purse is it, and that transfer is from/to same kind
    //    i.e. you can't transfer between abstract/concrete purses.
    //
    //    this is why I was thinking about the separation of the DSLs rather than having the `UnsupportedCheck` solution
    override fun thereIsATransfer(fromPurse: String, toPurse: String, value: Int, succeed: Boolean) {
        when (succeed) {
            true -> actionList.add(actionFactory.world.transferOkay(
                TransferDetails(fromPurse, toPurse, value.toULong()))
            )
            false -> actionList.add(actionFactory.world.transferLost(
                TransferDetails(fromPurse, toPurse, value.toULong()))
            )
        }
    }

    override fun thereIsNoTransfer() {
        actionList.add(actionFactory.world.noTransfer())
    }

    override fun parallel(vararg fns: MondexWhen.() -> Unit) {
        actionList.add(actionFactory.createParallelAction(
            fns.map { fn ->
                val w = MondexWhen(actionFactory)
                w.fn()
                w.actions()
            }
        ))
    }
}
