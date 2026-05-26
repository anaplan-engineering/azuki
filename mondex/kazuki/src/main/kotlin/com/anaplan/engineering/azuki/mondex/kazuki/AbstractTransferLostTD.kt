package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface AbstractTransferLostTD: AbstractWorldSecureOperation {

    @Invariant
    fun pursesAuthentic() = transferDetails.from in world.old.authPurses.dom
        && transferDetails.to in world.old.authPurses.dom

    @Invariant
    fun sufficientFundsProperty() = transferDetails.value <= world.old.authPurses[transferDetails.from].balance

    @Invariant
    fun pursesDistinct() = transferDetails.from != transferDetails.to

    @Invariant
    fun authPurseChanges() = world.dash.authPurses[transferDetails.from].balance == world.old.authPurses[transferDetails.from].balance - transferDetails.value
        && world.dash.authPurses[transferDetails.from].lost == world.old.authPurses[transferDetails.from].lost + transferDetails.value
        && world.dash.authPurses[transferDetails.to] == world.old.authPurses[transferDetails.to]

}
