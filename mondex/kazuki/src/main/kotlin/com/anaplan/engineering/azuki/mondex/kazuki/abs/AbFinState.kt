package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.World
import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Module

@Module
interface AbstractFinState {
    val abWorld: AbWorld
    val globalWorld: GlobalWorld

    @Invariant
    fun worldsMatch() = abWorld.properties.abAuthPurses == globalWorld.properties.gAuthPurses
}

@Module
interface GlobalWorld : World {

    @FunctionProvider(GlobalWorldProperties::class)
    val properties: GlobalWorldProperties
}

class GlobalWorldProperties(gWorld: GlobalWorld) {

    @Suppress("UNCHECKED_CAST")
    val gAuthPurses: Mapping<Name, AbPurse> = gWorld.purses as Mapping<Name, AbPurse>
    //val gAuthPurse: Mapping<Name, AbPurse> = abWorld.purses.filterValues { it is AbPurse }.mapValues { it.value as AbPurse }
}

