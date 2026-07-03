package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.check

interface WorldChecks {

    fun worldHasTotalBalance(worldName: String, balance: Int)

    fun worldHasPurse(worldName: String, purseName: String)

    infix fun String.hasTotalBalance(balance: Int) = worldHasTotalBalance(this, balance)
    infix fun String.hasPurse(purseName: String) = worldHasPurse(this, purseName)
}


