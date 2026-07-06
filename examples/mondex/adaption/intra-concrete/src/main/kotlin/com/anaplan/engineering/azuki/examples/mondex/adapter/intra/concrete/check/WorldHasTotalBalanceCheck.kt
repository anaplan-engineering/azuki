package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.specification.between.AuxWorld

class WorldHasTotalBalanceCheck (private val worldName: String, private val balance: Int) : ConcreteWorldCheck {

    override fun check(animation: ConcreteWorldAnimation) = true
//        (animation.getWorld(worldName) as? AuxWorld).properties.totalBalance == balance.toULong()

    override val behavior = -1
}
