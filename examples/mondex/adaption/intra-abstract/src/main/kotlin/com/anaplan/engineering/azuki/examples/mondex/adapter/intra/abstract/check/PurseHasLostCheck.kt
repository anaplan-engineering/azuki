package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation

class PurseHasLostCheck(private val purseName: String, private val lost: Int) : AbstractWorldCheck {

    override fun check(animation: AbstractWorldAnimation) =
        checkEquals(animation.getPurse(purseName).lost, lost.toULong())

    override val behavior = -1
}
