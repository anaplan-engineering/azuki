package com.anaplan.engineering.azuki.mondex.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.mondex.adapter.api.CreatePurseBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState

open class CreatePurseDeclarableAction(
    protected val purseName: String,
    protected val purse: Purse,
) : CreatePurseBehaviour(), DeclarableAction<MondexDeclarationState> {

    override fun declare(state: MondexDeclarationState) = state.declarePurse(purseName, purse)
}
