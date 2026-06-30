package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Module

@Module
interface GlobalWorld {
    val gAuthPurse: Mapping<Name, AbPurse>
}

@Module
interface AbstractFinState {
    val abWorld: AbWorld
    val globalWorld: GlobalWorld

    @Invariant
    fun worldsMatch() = abWorld.abAuthPurse == globalWorld.gAuthPurse
}


