package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.ParallelWhen
import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetailsBuilder
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetailsBuilder
import com.anaplan.engineering.azuki.mondex.dsl.action.MondexActions

class MondexWhen(private val actionFactory: MondexActionFactory<*>) :
    When<MondexActionFactory<*>>,
    ParallelWhen<MondexActionFactory<*>, MondexWhen>,
    MondexActions {

    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

    override fun parallel(vararg fns: MondexWhen.() -> Unit) {
        actionList.add(actionFactory.createParallelAction(
            fns.map { fn ->
                val w = MondexWhen(actionFactory)
                w.fn()
                w.actions()
            }
        ))
    }

    override fun makeAbTransfer(transferDetails: TransferDetailsBuilder.() -> Unit) {
        val details = TransferDetailsBuilder()
            .apply(transferDetails)
            .build()
        require(details.from != details.to) { "Cannot transfer to self" }
        require(details.value > 0UL) { "Must transfer a positive amount" }
        actionList.add(actionFactory.protocol.transferOkay(details))
    }

    override fun makeAbTransferAlt(transferDetails: TransferDetails) {
        TODO("Choose one and keep the otehr")
    }

    override fun loseAbTransfer(transferDetails: TransferDetailsBuilder.() -> Unit) {
        val details = TransferDetailsBuilder()
            .apply(transferDetails)
            .build()
        require(details.from != details.to) { "Cannot transfer to self" }
        require(details.value > 0UL) { "Must transfer a positive amount" }
        actionList.add(actionFactory.protocol.transferLost(details))
    }

    override fun noTransfer() {
        actionList.add(actionFactory.protocol.ignore())
    }

    override fun makeConTransfer(payDetails: PayDetailsBuilder.() -> Unit) {
        val details = PayDetailsBuilder()
            .apply(payDetails)
            .build()
        require(details.td.from != details.td.to) { "Cannot transfer to self" }
        require(details.td.value > 0UL) { "Must transfer a positive amount" }
        //actionList.add(actionFactory.protocol.transferLost(details))
        TODO("Not sure will be needed")
    }
}
