package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.toName

class WorldHasPurseCheck(private val worldName: String, private val purseName: String) : AbstractWorldCheck {

    override fun check(animation: AbstractWorldAnimation) =
        checkTrue(purseName.toName() in animation.getWorld(worldName).abAuthPurse.dom)

    override val behavior = -1
}
