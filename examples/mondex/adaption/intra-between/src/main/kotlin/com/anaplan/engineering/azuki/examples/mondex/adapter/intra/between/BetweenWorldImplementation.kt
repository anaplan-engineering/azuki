package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action.BetweenWorldAction
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldImplementation

class BetweenWorldImplementation : IntraWorldImplementation<BetweenWorldAction> {
    override val name = "Between"

    override val implementationDefaults = NoSystemDefaults

    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = BetweenWorldSystemFactory()
}
