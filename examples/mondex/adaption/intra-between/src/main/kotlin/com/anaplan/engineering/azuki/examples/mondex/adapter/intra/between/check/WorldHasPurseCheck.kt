package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.specification.between.toName

class WorldHasPurseCheck(private val worldName: String, private val purseName: String) : WorldCheck {

    override fun check(animation: WorldAnimation<*,*>) =
        checkTrue(purseName.toName() in animation.getWorld(worldName).conAuthPurse.dom)

    override val behavior = -1
}
