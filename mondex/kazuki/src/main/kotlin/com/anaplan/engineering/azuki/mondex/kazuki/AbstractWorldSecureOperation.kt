package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

abstract class AbstractWorldSecureOperation(world: Delta<World>, aQ: AIN, aE: AOUT, val transferDetails: TransferDetails): AbstractOperation(world, aQ, aE) {
    @Invariant
    fun aQInRangeOfTransfer() = aQ is transfer

    @Invariant
    fun transferDetailsMatchInput() = aQ is transfer && aQ.transferDetails == transferDetails

    @Invariant
    fun authPurseOtherwiseUnchanged() = world.dash.authPurses.domSubtract(
        mk_Set(transferDetails.from, transferDetails.to)) == world.old.authPurses.domSubtract(
        mk_Set(transferDetails.from, transferDetails.to))
}
