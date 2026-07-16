package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation

class PurseHasBalanceCheck(private val purseName: String, private val balance: Int) : WorldCheck {

    override fun check(animation: WorldAnimation<*,*>) =
        checkEquals(animation.getPurse(purseName).balance, balance.toULong())

    override val behavior = -1
}
