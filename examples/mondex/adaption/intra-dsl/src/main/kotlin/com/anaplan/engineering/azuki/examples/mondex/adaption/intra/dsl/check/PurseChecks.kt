package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.check

interface PurseChecks {

    fun purseHasBalance(purseName: String, balance: Int)

    fun purseHasLost(purseName: String, lost: Int)

    fun purseExLogContains(purseName: String, transferName: String)

    infix fun String.hasBalance(balance: Int) = purseHasBalance(this, balance)
    infix fun String.hasLost(lost: Int) = purseHasLost(this, lost)
    infix fun String.exceptionLogContains(transferName: String) = purseExLogContains(this, transferName)

}
