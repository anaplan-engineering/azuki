package com.anaplan.engineering.azuki.rightofway.adapter.vdm

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayImplementation

class VdmRightOfWayImplementation: RightOfWayImplementation {

    override val name = "VDM"

    override val implementationDefaults = NoSystemDefaults

    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = VdmSystemFactory()
}
