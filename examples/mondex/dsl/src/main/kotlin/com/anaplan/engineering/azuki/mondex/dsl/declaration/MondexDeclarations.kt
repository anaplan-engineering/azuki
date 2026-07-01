package com.anaplan.engineering.azuki.mondex.dsl.declaration

import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.Status
import com.anaplan.engineering.azuki.mondex.dsl.block.AbWorldBlock
import com.anaplan.engineering.azuki.mondex.dsl.block.ConWorldBlock

interface ModexDeclarations {
    fun thereIsAnAbstractWorld(name: String, init: (AbWorldBlock.() -> Unit))
    fun thereIsAConcreteWorld(name: String, init: (ConWorldBlock.() -> Unit))
}
