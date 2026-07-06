package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation

class WorldHasTotalValueCheck(private val worldName: String, private val value: Int) : AbstractWorldCheck {

    override fun check(animation: AbstractWorldAnimation) =
        checkEquals(animation.getWorld(worldName).properties.totalValue, value.toULong())

    //LF QST: why should this be unsupportedBehaviour?
    override val behavior = -1
}
