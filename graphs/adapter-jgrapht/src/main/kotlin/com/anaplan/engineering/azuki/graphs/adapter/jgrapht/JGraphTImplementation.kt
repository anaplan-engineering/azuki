package com.anaplan.engineering.azuki.graphs.adapter.jgrapht

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphImplementation

class JGraphTImplementation : GraphImplementation {
    override val name = "JGraphT"
    override val implementationDefaults = NoSystemDefaults
    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter
    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = JGraphTSystemFactory()
}
