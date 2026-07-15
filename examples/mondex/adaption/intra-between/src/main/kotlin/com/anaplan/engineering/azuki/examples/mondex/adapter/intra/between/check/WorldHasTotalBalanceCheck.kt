package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation

class WorldHasTotalBalanceCheck (private val worldName: String, private val balance: Int) : BetweenWorldCheck {

    override fun check(animation: BetweenWorldAnimation) = true
//        (animation.getWorld(worldName) as? AuxWorld).properties.totalBalance == balance.toULong()

    override val behavior = -1
}
