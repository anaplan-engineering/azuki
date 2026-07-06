package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation

class WorldHasTotalLostCheck(private val worldName: String, private val lost: Int) : AbstractWorldCheck {

    override fun check(animation: AbstractWorldAnimation) =
        checkEquals(animation.getWorld(worldName).properties.totalLost, lost.toULong())

    //LF QST: why should this be unsupportedBehaviour?
    override val behavior = -1
}
