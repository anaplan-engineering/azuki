package com.anaplan.engineering.azuki.mondex.dsl.declaration

import com.anaplan.engineering.azuki.mondex.dsl.WorldBlock

interface PurseDeclarations {
    fun thereIsAPurse(balance: ULong, lost: ULong)
}

interface WorldDeclarations {
    fun thereIsAWorld(init: WorldBlock.() -> Unit)
}
