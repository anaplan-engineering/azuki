package com.anaplan.engineering.azuki.graphs.adapter.jung

import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphImplementation

class JungImplementation : GraphImplementation {
    override val name = "Jung"
    override val total = false
    override val implementationDefaults = NoSystemDefaults
    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = JungSystemFactory()
}
