package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.CreatePurseBehaviour
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.CreateTransferBehaviour
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.IntraWorldDeclarationState

abstract class CreateTransferDeclarableAction(
    protected val transferName: String,
    protected val fromPurse: String,
    protected val toPurse: String,
    protected val amount: Int
) : CreateTransferBehaviour(), DeclarableAction<IntraWorldDeclarationState> {

    override fun declare(state: IntraWorldDeclarationState) = state.declareTransfer(transferName, fromPurse, toPurse, amount)
}
