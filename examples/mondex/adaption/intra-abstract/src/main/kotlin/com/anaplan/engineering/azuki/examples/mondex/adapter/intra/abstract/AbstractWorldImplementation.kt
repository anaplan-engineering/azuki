package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action.AbstactWorldAction
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldImplementation

class AbstractWorldImplementation : IntraWorldImplementation<AbstactWorldAction> {
    override val name = "Abstract"

    override val implementationDefaults = NoSystemDefaults

    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = AbstractWorldSystemFactory()
}
