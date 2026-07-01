package com.anaplan.engineering.azuki.mondex.adapter.kazuki.action

import com.anaplan.engineering.azuki.mondex.adapter.api.TransferBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetails
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.abs.transfer

class TransferOkayAction(
    val transferDetails: TransferDetails,
    val worldName: String = DEFAULT_WORLD,
) : TransferBehaviour(), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        val td = mk_TransferDetails(transferDetails.from, transferDetails.to, transferDetails.value)
        env.set(worldName, env.world(worldName).functions.abTransferOkayTD(transfer(td), td))
    }

    companion object {
        private const val DEFAULT_WORLD = com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState.DEFAULT_WORLD
    }
}
