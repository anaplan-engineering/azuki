package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation

class PurseHasBalanceCheck(private val purseName: String, private val balance: Int) : BetweenWorldCheck {

    override fun check(animation: BetweenWorldAnimation) =
        checkEquals(animation.getPurse(purseName).balance, balance.toULong())

    override val behavior = -1
}
