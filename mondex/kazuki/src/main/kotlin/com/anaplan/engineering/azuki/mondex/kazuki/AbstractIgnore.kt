package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

class AbstractIgnore(world: Delta<World>, aQ: AIN, aE: AOUT): AbstractOperation(world, aQ, aE) {
    @Invariant
    fun authPurseDoesntChange() = world.old.authPurses == world.dash.authPurses
}
