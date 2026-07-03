package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.definition

interface TransferDefinition {

    fun thereIsATransfer(transferName: String, fromPurse: String, toPurse: String, amount: Int)
}
