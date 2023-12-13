package com.anaplan.engineering.azuki.graphs.adapter.jung

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphImplementation

class JungImplementation : GraphImplementation {
    override val name = "Jung"
    override val implementationDefaults = NoSystemDefaults
    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter
    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = JungSystemFactory()
}
