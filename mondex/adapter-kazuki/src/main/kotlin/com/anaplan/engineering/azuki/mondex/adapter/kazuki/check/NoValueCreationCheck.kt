package com.anaplan.engineering.azuki.mondex.adapter.kazuki.check

import com.anaplan.engineering.azuki.mondex.adapter.api.NoValueCreationBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment

class NoValueCreationCheck(
    private val expected: Boolean,
) : NoValueCreationBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val beforeWorld = env.world(MondexDeclarationState.BEFORE_DEFAULT_WORLD)
        val world = env.world(MondexDeclarationState.DEFAULT_WORLD)

        //LF @EK this is not quite right. The check is against a before and after state.
//        val noCreation = world.functions.noValueCreation(beforeWorld.authPurses)
//
//        return noCreation == expected
        return false
    }
}
