package com.anaplan.engineering.azuki.rightofway.adapter.implementation

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayImplementation

class SampleRightOfWayImplementation() : RightOfWayImplementation {
    override val name = "SampleImpl"
    override val implementationDefaults = NoSystemDefaults
    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = SampleSystemFactory()

}
