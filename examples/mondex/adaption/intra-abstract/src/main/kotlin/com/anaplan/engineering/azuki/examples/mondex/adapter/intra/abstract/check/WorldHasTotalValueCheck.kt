package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation

class WorldHasTotalValueCheck(private val worldName: String, private val value: Int) : AbstractWorldCheck {

    override fun check(animation: AbstractWorldAnimation) =
        checkEquals(animation.getWorld(worldName).abAuthPurse.rng.sumOf { it.balance + it.lost }, value.toULong())

    override val behavior = -1
}
