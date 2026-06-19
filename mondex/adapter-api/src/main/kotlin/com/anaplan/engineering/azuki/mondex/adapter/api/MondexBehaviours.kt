package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object MondexBehaviours {
    const val CreatePurse = 1
    const val CreateWorld = 2
    const val PurseExists = 3
    const val WorldExists = 4
}

open class CreatePurseBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.CreatePurse
}

open class CreateWorldBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.CreateWorld
}

open class TransferBehaviour : ReifiedBehavior {
    override val behavior = MondexFunctions.AbsTransfer
}

open class IgnoreBehaviour : ReifiedBehavior {
    override val behavior = MondexFunctions.AbsIgnore
}

open class PurseExistsBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.PurseExists
}

open class WorldExistsBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.WorldExists
}
