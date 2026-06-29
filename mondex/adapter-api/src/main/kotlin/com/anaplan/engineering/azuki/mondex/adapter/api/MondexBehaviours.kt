package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

object MondexBehaviours {
    const val CreatePurse = 1
    const val CreateWorld = 2
    const val PurseExists = 3
    const val WorldExists = 4
    const val TransferBehaviour = 5
    const val IgnoreBehaviour = 6
    const val NoValueCreationBehaviour = 7
    const val AllValueAccountedBehaviour = 8
}

open class CreatePurseBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.CreatePurse
}

open class CreateWorldBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.CreateWorld
}

open class TransferBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.TransferBehaviour
}

open class IgnoreBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.IgnoreBehaviour
}

open class PurseExistsBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.PurseExists
}

open class WorldExistsBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.WorldExists
}

open class NoValueCreationBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.NoValueCreationBehaviour
}

open class AllValueAccountedBehaviour : ReifiedBehavior {
    override val behavior = MondexBehaviours.AllValueAccountedBehaviour
}
