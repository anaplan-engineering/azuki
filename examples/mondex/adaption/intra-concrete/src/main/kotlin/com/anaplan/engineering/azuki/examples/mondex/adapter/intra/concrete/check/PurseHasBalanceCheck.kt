package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation

class PurseHasBalanceCheck(private val purseName: String, private val balance: Int) : ConcreteWorldCheck {

    override fun check(animation: ConcreteWorldAnimation) =
        checkEquals(animation.getPurse(purseName).balance, balance.toULong())

    override val behavior = -1
}
