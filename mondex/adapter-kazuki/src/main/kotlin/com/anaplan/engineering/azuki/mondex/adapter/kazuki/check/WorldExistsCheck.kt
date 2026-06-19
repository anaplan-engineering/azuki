package com.anaplan.engineering.azuki.mondex.adapter.kazuki.check

import com.anaplan.engineering.azuki.mondex.adapter.api.WorldExistsBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.toMapping
import com.anaplan.engineering.kazuki.core.mk_Mapping

class WorldExistsCheck(
    private val authPurses: Map<String, Pair<ULong, ULong>>,
    private val expected: Boolean,
) : WorldExistsBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val exists = authPurses.toMapping() == env.world(MondexDeclarationState.DEFAULT_WORLD).authPurses
        return exists == expected
    }
}
