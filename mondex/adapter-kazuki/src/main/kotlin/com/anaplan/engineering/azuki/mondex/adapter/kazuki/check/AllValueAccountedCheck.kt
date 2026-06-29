package com.anaplan.engineering.azuki.mondex.adapter.kazuki.check

import com.anaplan.engineering.azuki.mondex.adapter.api.AllValueAccountedBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment

class AllValueAccountedCheck(
    private val expected: Boolean,
) : AllValueAccountedBehaviour(), KazukiCheck  {

    override fun check(env: ExecutionEnvironment): Boolean {
        val beforeWorld = env.world(MondexDeclarationState.BEFORE_DEFAULT_WORLD)
        val world = env.world(MondexDeclarationState.DEFAULT_WORLD)

        val allValueAccounted = world.functions.allValueAccounted(beforeWorld.authPurses)

        return allValueAccounted == expected
    }
}
