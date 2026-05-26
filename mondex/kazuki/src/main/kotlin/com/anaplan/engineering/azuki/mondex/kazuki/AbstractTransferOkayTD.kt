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

    @Invariant
    fun authPurseChanges() = world.dash.authPurses[transferDetails.from].balance == world.old.authPurses[transferDetails.from].balance - transferDetails.value
        && world.dash.authPurses[transferDetails.to].balance == world.old.authPurses[transferDetails.to].balance + transferDetails.value
        && world.dash.authPurses[transferDetails.from].lost == world.old.authPurses[transferDetails.from].lost
        && world.dash.authPurses[transferDetails.to].lost == world.old.authPurses[transferDetails.to].lost


}

