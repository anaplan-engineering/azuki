package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation

class WorldHasTotalLostCheck(private val worldName: String, private val lost: Int) : WorldCheck {

    override fun check(animation: WorldAnimation<*,*>) = true

    override val behavior = -1
}
