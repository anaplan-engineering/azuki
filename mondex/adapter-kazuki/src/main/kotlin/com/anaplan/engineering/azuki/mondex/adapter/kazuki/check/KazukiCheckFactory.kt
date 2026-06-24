package com.anaplan.engineering.azuki.mondex.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.api.PurseCheckFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.WorldCheckFactory
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment

class KazukiCheckFactory : MondexCheckFactory {
    override val purse = KazukiPurseCheckFactory
    override val world = KazukiWorldCheckFactory

    override fun systemValid() = object : KazukiCheck {
        override val behavior = unsupportedBehavior
        override fun check(env: ExecutionEnvironment) = true
    }
}

object KazukiPurseCheckFactory : PurseCheckFactory {
    override fun purseExists(personName: String, result: Boolean) =
        PurseExistsCheck(personName, result)

    override fun purseExists(personName: String, balance: ULong, lost: ULong, result: Boolean) =
        PurseExistsWithValuesCheck(personName, balance, lost, result)
}

object KazukiWorldCheckFactory : WorldCheckFactory {
    override fun worldExists(authPurses: Map<String, Purse>, result: Boolean) =
        WorldExistsCheck(authPurses, result)

    override fun noValueCreation(result: Boolean) = NoValueCreationCheck(result)

    override fun allValueAccounted(result: Boolean) = AllValueAccountedCheck(result)
}

interface KazukiCheck : Check {
    fun check(env: ExecutionEnvironment): Boolean
}
