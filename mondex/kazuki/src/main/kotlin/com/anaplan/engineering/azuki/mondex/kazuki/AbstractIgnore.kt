package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface AbstractIgnore: AbstractOperation {
    @Invariant
    fun authPurseDoesntChange() = world.old.authPurses == world.dash.authPurses
}
