package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.totalBalance
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.totalValue

class WorldHasTotalBalanceCheck(private val worldName: String, private val balance: Int) : AbstractWorldCheck {

    override fun check(animation: AbstractWorldAnimation) =
        checkEquals(animation.getWorld(worldName).abAuthPurse.totalBalance(), balance.toULong())

    //LF QST: why should this be unsupportedBehaviour?
    override val behavior = -1
}
