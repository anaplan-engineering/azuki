package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.ReifiedBehavior

enum class IntraWorldBehaviour {
    CreatePurse,
    CreateWorld,
    CreateTransfer,
    RequestTransfer,
    SendTransfer,
    AcknowledgeTransfer,
    AbortTransfer,
    AddPurseToWorld,
}

abstract class IntraWorldReifiedBehaviour(val intraWorldBehaviour: IntraWorldBehaviour) : ReifiedBehavior {
    override val behavior: Behavior = intraWorldBehaviour.ordinal
}

open class CreatePurseBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.CreatePurse)
open class AddPurseToWorldBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.AddPurseToWorld)
open class CreateWorldBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.CreateWorld)
open class CreateTransferBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.CreateTransfer)
open class RequestTransferBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.RequestTransfer)
open class SendTransferBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.SendTransfer)
open class AcknowledgeTransferBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.AcknowledgeTransfer)
open class AbortTransferBehaviour : IntraWorldReifiedBehaviour(IntraWorldBehaviour.AbortTransfer)



