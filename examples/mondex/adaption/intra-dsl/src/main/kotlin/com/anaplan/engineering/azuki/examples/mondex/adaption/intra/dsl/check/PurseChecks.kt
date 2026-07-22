package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.check

import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.TransferRef

interface PurseChecks {

    fun purseHasBalance(purseName: String, balance: Int)

    fun purseHasLost(purseName: String, lost: Int)

    fun purseExLogContains(purseName: String, transfer: TransferRef)

    infix fun String.hasBalance(balance: Int) = purseHasBalance(this, balance)
    infix fun String.hasLost(lost: Int) = purseHasLost(this, lost)
    infix fun String.exceptionLogContains(transfer: TransferRef) = purseExLogContains(this, transfer)

}
