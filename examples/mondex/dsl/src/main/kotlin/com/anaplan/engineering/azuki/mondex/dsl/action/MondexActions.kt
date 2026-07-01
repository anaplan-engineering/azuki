package com.anaplan.engineering.azuki.mondex.dsl.action

import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetailsBuilder
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetailsBuilder

interface MondexActions {
    // AbTransferOkay
    fun makeAbTransfer(transferDetails: TransferDetailsBuilder.() -> Unit)
    fun makeAbTransferAlt(transferDetails: TransferDetails)
    // AbTransferLost
    fun loseAbTransfer(transferDetails: TransferDetailsBuilder.() -> Unit)
    // AbIgnore
    fun noTransfer()

    // Concrete transferOkay?
    fun makeConTransfer(payDetails: PayDetailsBuilder.() -> Unit)
}

