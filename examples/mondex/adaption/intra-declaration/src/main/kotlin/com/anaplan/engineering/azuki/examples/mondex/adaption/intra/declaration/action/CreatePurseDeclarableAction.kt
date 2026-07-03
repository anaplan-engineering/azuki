package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.CreatePurseBehaviour
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.IntraWorldDeclarationState

abstract class CreatePurseDeclarableAction(
    protected val purseName: String,
    protected val balance: Int,
) : CreatePurseBehaviour(), DeclarableAction<IntraWorldDeclarationState> {

    override fun declare(state: IntraWorldDeclarationState) = state.declarePurse(purseName, balance)
}
