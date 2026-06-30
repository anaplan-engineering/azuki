package com.anaplan.engineering.azuki.mondex.adapter.kazuki.check

import com.anaplan.engineering.azuki.mondex.adapter.api.PurseExistsBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment

class PurseExistsCheck(
    private val personName: String,
    private val expected: Boolean,
) : PurseExistsBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val exists = personName in env.world(MondexDeclarationState.DEFAULT_WORLD).abAuthPurse.dom
        return exists == expected
    }
}

class PurseExistsWithValuesCheck(
    private val personName: String,
    private val balance: ULong,
    private val lost: ULong,
    private val expected: Boolean,
) : PurseExistsBehaviour(), KazukiCheck {

    override fun check(env: ExecutionEnvironment): Boolean {
        val exists = personName in env.world(MondexDeclarationState.DEFAULT_WORLD).abAuthPurse.dom
            && env.world(MondexDeclarationState.DEFAULT_WORLD).abAuthPurse[personName].balance == balance
            && env.world(MondexDeclarationState.DEFAULT_WORLD).abAuthPurse[personName].lost == lost
        return exists == expected
    }
}
