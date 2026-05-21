package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object MondexBehaviours {
    const val CreatePurse = 1
}

open class CreatePurseBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.CreatePurse
}
