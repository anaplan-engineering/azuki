package com.anaplan.engineering.azuki.vdm

import com.anaplan.engineering.azuki.core.system.Action

interface VdmAction<SC: SystemContext> : Action {
    fun build(builder: ModuleBuilder<SC>): ModuleBuilder<SC>
}

typealias DefaultVdmAction = VdmAction<EmptySystemContext>


val toDefaultVdmAction: (Action) -> DefaultVdmAction = {
    @Suppress("UNCHECKED_CAST")
    it as? DefaultVdmAction ?: throw IllegalArgumentException("Invalid action: $it")
}

