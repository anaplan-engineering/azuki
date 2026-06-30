package com.anaplan.engineering.azuki.mondex.adapter.kazuki.check

import com.anaplan.engineering.azuki.mondex.adapter.api.AbPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.ConPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.api.WorldExistsBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.toAbMapping
import kotlin.collections.filterValues

class WorldExistsCheck(
    private val authPurses: Map<String, Purse>,
    private val expected: Boolean,
) : WorldExistsBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        //LF @EK not sure this is the best/right way, but keeping it for now
        @Suppress("UNCHECKED_CAST")
        val abPurses: Map<String, AbPurse> = authPurses.filterValues { it is AbPurse } as Map<String, AbPurse>
        @Suppress("UNCHECKED_CAST")
        val conPurses: Map<String, ConPurse> = authPurses.filterValues { it is ConPurse } as Map<String, ConPurse>
        require(abPurses.isEmpty() || abPurses.size == authPurses.size) { "All purses must be of the same type" }
        require(conPurses.isEmpty() || conPurses.size == authPurses.size) { "All purses must be of the same type" }

        //LF @EK needs if-then-else for Con/AbPurse and Con/AbWorld
        val exists = abPurses.toAbMapping() == env.world(MondexDeclarationState.DEFAULT_WORLD).abAuthPurse
        return exists == expected
    }
}
