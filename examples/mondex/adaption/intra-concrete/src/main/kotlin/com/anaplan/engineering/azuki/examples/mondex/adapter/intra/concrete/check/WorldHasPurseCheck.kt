package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.specification.between.toName

class WorldHasPurseCheck(private val worldName: String, private val purseName: String) : ConcreteWorldCheck {

    override fun check(animation: ConcreteWorldAnimation) =
        checkTrue(purseName.toName() in animation.getWorld(worldName).conAuthPurse.dom)

    override val behavior = -1
}
