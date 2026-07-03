package com.anaplan.engineering.azuki.examples.mondex.specification.concrete

import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Name
import com.anaplan.engineering.kazuki.core.*

// we need powerset though: careful with the Z peculiarity about sets as types



@Module
interface ConWorld {
    val conAuthPurse: InjectiveMapping<Name, ConPurse>
    val ether: Set<Message>
    val archive: LogBook

    @Invariant
    fun nameInjective() =
        forall(conAuthPurse) { (n, p) -> p.name == n }

    @Invariant
    fun logDetailsForKnownPurses() =
        forall(archive) { nld -> nld._1 in conAuthPurse.dom }
}







