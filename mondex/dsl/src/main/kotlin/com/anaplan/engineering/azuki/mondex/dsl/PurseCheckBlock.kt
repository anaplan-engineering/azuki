package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory

@ScenarioDsl
class PurseCheckBlock {
    private var b: ULong? = null
    private var l: ULong? = null

    fun getBalance(): ULong? = b
    fun getLost(): ULong? = l

    fun hasBalance(balance: Int) {
        require(balance >= 0) { "balance can't be negative" }
        b = balance.toULong()
    }

    fun hasLost(lost: Int) {
        require(lost >= 0) { "lost can't be negative" }
        l = lost.toULong()
    }
}
