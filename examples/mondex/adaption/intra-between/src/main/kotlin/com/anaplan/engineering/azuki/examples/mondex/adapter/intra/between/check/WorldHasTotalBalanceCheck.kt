package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation

class WorldHasTotalBalanceCheck (private val worldName: String, private val balance: Int) : WorldCheck {

    // TODO EK Fill in when operations available
    override fun check(animation: WorldAnimation<*,*>) = false
//        (animation.getWorld(worldName) as? AuxWorld).properties.totalBalance == balance.toULong()

    override val behavior = -1
}
