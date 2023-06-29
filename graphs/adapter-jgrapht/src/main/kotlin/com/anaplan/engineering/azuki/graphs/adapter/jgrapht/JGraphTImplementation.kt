package com.anaplan.engineering.azuki.graphs.adapter.jgrapht

import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphImplementation

class JGraphTImplementation : GraphImplementation {
    override val name = "JGraphT"
    override val total = false
    override val implementationDefaults = NoSystemDefaults
    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = JGraphTSystemFactory()
}
