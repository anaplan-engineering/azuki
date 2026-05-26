package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

class AbstractFinState(val world: World, val globalWorld: GlobalWorld) {
    @Invariant
    fun worldsMatch() = world.authPurses == globalWorld.gAuthPurses
}
