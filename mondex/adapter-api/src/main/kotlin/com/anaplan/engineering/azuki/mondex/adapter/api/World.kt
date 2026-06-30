package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.runner.Log
import kotlin.collections.component1
import kotlin.collections.component2

sealed class World (
    //LF @EK maybe have this here?
    // protected val name: Name,
    protected val purses: Map<String, Purse>
)

data class AbWorld(val abPurses: Map<String, AbPurse>) : World(abPurses)

//LF @EK missing ether and archieve
data class ConWorld(val conPurses: Map<String, ConPurse>) : World(conPurses)
