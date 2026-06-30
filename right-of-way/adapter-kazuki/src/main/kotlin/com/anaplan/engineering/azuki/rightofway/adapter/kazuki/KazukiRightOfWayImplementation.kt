package com.anaplan.engineering.azuki.rightofway.adapter.kazuki

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayImplementation

class KazukiRightOfWayImplementation : RightOfWayImplementation {
    override val name = "Kazuki"

    override val implementationDefaults = NoSystemDefaults

    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = KazukiSystemFactory()

}
