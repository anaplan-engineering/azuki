package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem_Module.mk_BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld_Module.mk_BetweenWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook_Module.mk_LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.mk_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.mk_ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.azuki.examples.mondex.specification.mondexPretty
import com.anaplan.engineering.kazuki.core.InjectiveMapping
import com.anaplan.engineering.kazuki.core.as_InjectiveMapping
import com.anaplan.engineering.kazuki.core.as_Mapping
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Mapping
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.toNat
import kotlin.test.Test
import kotlin.test.assertTrue

const val person1 = "person1"
const val person2 = "person2"

class BetweenWorldProtocolRun {

    val ether = mk_Set(Bottom, ReadExceptionLog)

    fun createPayDetails(from: String, to: String, balance: Int): PayDetails {
        require(from != to)
        return mk_PayDetails(from.toName(), to.toName(), balance.toNat(), 0UL, 0UL)
    }

    fun createPurse(name: String, balance: Int) = mk_ConPurse(balance.toNat(), mk_Set(), name.toName(), 0UL, null, Status.eaFrom)
    fun createPurses(decls: Set<Pair<String, Int>>): InjectiveMapping<Name, ConPurse> {
        require(decls.map { it.first }.toSet().size == decls.size)
        return as_InjectiveMapping(decls.map { (n, b) -> mk_(n.toName(), createPurse(n, b)) })
    }

//    StartFrom::class -> mk_StartFrom(
//    mk_CounterPartyDetails(toPurse.name, pd.value, toPurse.nextSeqNo)
//    ) as T
//    StartTo::class -> mk_StartTo(
//    mk_CounterPartyDetails(fromPurse.name, pd.value, fromPurse.nextSeqNo)
//    ) as T

    @Test
    fun startFromRequest() {
        val pd = createPayDetails("from", "to", 2)
        val purses = createPurses(setOf("from" to 3, "to" to 3))
        var system = mk_BetweenSystem(mk_BetweenWorld(purses, ether, mk_LogBook()), Bottom)
        system = system.functions.startTransferFrom(pd.from, pd.startFrom())
        system = system.functions.startTransferTo(pd.to, pd.startTo())
        system = system.functions.requestTransfer(pd.from, system.last)
        system = system.functions.sendTransfer(pd.to, system.last)
        system = system.functions.ackTransfer(pd.from, system.last)
        println(system.mondexPretty())
    }
}
