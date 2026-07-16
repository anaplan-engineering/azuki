package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation

class WorldHasTotalLostCheck(private val worldName: String, private val lost: Int) : WorldCheck {

    // TODO EK Fill in when operations available
    override fun check(animation: WorldAnimation<*,*>) = false

    override val behavior = -1
}
