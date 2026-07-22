package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation

class PurseExLogContains(
    private val purseName: String,
    private val transferName: String
) : WorldCheck {

    override fun check(animation: WorldAnimation<*,*>): Boolean {
        return checkTrue(animation.getTransfer(transferName).payDetails in animation.getPurse(purseName).exLog)
    }

    override val behavior = -1
}
