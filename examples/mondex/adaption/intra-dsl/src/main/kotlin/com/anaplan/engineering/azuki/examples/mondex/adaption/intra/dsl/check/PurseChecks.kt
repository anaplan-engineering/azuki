package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.check

interface PurseChecks {

    fun purseHasBalance(purseName: String, balance: Int)

    fun purseHasLost(purseName: String, lost: Int)

    infix fun String.hasBalance(balance: Int) = purseHasBalance(this, balance)
    infix fun String.hasLost(lost: Int) = purseHasLost(this, lost)

}
