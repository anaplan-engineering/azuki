package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.TransferRef
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.toName

class PurseExLogContains(
    private val purseName: String,
    private val transfer: TransferRef,
) : WorldCheck {

    override fun check(animation: WorldAnimation<*,*>): Boolean {
        val exLog = animation.getPurse(purseName).exLog
        return checkTrue(
            exLog.any { it.matches(transfer) },
            message = transfer.name,
        )
    }

    override val behavior = -1
}

private fun PayDetails.matches(ref: TransferRef): Boolean =
    from == ref.from.toName() &&
        to == ref.to.toName() &&
        value == ref.amount.toULong()
