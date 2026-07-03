package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.action

interface TransferActions : TransferDeclarableActions {
    fun requestTransfer(transferName: String)
    fun sendTransfer(transferName: String)
    fun acknowledgeTransfer(transferName: String)
    fun abortTransfer(transferName: String)
}

interface TransferDeclarableActions {
    fun createTransfer(worldName: String?, transferName: String, fromPurse: String, toPurse: String, amount: Int)
}
