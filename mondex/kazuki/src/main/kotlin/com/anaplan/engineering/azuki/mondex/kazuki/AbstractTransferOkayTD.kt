package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

class AbstractTransferOkayTD(world: Delta<World>, aQ: AIN, aE: AOUT, transferDetails: TransferDetails):
    AbstractWorldSecureOperation(world, aQ, aE, transferDetails) {

    @Invariant
    fun pursesAuthentic() = transferDetails.from in world.old.authPurses.dom
        && transferDetails.to in world.old.authPurses.dom

    @Invariant
    fun sufficientFundsProperty() = transferDetails.value <= world.old.authPurses[transferDetails.from].balance

    @Invariant
    fun pursesDistinct() = transferDetails.from != transferDetails.to


}

