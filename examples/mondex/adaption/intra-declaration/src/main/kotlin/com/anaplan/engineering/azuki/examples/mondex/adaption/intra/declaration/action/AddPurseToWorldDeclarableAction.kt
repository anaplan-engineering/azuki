package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.CreatePurseBehaviour
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.IntraWorldDeclarationState

abstract class AddPurseToWorldDeclarableAction(
    protected val worldName: String,
    protected val purseName: String,
) : CreatePurseBehaviour(), DeclarableAction<IntraWorldDeclarationState> {

    override fun declare(state: IntraWorldDeclarationState) = state.addPurseToWorld(worldName, purseName)}

