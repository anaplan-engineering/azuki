package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action.ConcreteWorldAction
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldImplementation

class ConcreteWorldImplementation : IntraWorldImplementation<ConcreteWorldAction> {
    override val name = "Concrete"

    override val implementationDefaults = NoSystemDefaults

    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = ConcreteWorldSystemFactory()
}
