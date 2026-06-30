package com.anaplan.engineering.azuki.mondex.adapter.kazuki.action

import com.anaplan.engineering.azuki.mondex.adapter.api.AbPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.declaration.action.CreatePurseDeclarableAction
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.withPurse

// This should be the adapter's purse input, not Kazuki's, then  create corresponding
class CreatePurseAction(
    purseName: String,
    purse: Purse,
) : CreatePurseDeclarableAction(purseName, purse), KazukiAction {

    //LF @EK here we need a differentiator between what kind of purse/world to create
    override fun act(env: ExecutionEnvironment) {
        val abPurse = purse as AbPurse
        env.set(MondexDeclarationState.DEFAULT_WORLD,
            env.world(MondexDeclarationState.DEFAULT_WORLD)
                .withPurse(purseName, abPurse))
    }
}
