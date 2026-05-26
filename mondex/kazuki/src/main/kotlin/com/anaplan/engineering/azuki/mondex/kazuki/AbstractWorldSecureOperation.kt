package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface AbstractWorldSecureOperation: AbstractOperation {
    val transferDetails: TransferDetails

    @Invariant
    fun aQInRangeOfTransfer() = aQ is transfer

    @Invariant
    fun transferDetailsMatchInput() = (aQ as transfer).transferDetails == transferDetails

    @Invariant
    fun authPurseOtherwiseUnchanged() = world.dash.authPurses.domSubtract(
        mk_Set(transferDetails.from, transferDetails.to)) == world.old.authPurses.domSubtract(
        mk_Set(transferDetails.from, transferDetails.to))
}
