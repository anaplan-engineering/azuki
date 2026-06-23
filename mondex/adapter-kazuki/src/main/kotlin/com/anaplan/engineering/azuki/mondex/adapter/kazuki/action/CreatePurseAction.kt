package com.anaplan.engineering.azuki.mondex.adapter.kazuki.action

import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.declaration.action.CreatePurseDeclarableAction
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.withPurse

class CreatePurseAction(
    purseName: String,
    purse: Purse,
) : CreatePurseDeclarableAction(purseName, purse), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        env.set(MondexDeclarationState.DEFAULT_WORLD, env.world(MondexDeclarationState.DEFAULT_WORLD).withPurse(purseName, purse))
    }
}
