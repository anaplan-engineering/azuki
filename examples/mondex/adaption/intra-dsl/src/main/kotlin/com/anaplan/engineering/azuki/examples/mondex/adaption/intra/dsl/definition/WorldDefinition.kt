package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.definition

import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl.WorldActionBlock

interface WorldDefinition {

    fun thereIsAWorld(worldName: String, init: (WorldActionBlock.() -> Unit)? = null)
}
