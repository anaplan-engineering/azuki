package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation

class PurseHasBalanceCheck(private val purseName: String, private val balance: Int) : AbstractWorldCheck {

    override fun check(animation: AbstractWorldAnimation) =
        checkEquals(animation.getPurse(purseName).balance, balance.toULong())

    override val behavior = -1
}
