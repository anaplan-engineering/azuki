package com.anaplan.engineering.azuki.mondex.kazuki.betw

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.World
import com.anaplan.engineering.kazuki.core.*

// we need powerset though: careful with the Z peculiarity about sets as types
typealias LogBook = Relation<Name, PayDetails>

@Module
interface ConWorld : World {
    val ether: Set<Message>
    val archive: LogBook

    @Invariant
    fun nameInjective() =
        forall(properties.conAuthPurse.dom) { n -> properties.conAuthPurse[n].name == n }

    @Invariant
    fun logDetailsForKnownPurses() =
        forall(archive) { nld -> nld._1 in properties.conAuthPurse.dom }

    @FunctionProvider(ConWorldFunctions::class)
    val functions: ConWorldFunctions

    @FunctionProvider(ConWorldProperties::class)
    val properties: ConWorldProperties
}

@Module
interface AuxWorld : ConWorld {
    // AuxWorld extra fields are properties of constructed ones
    //LF @QST can I do this here? want to extend the world's properties
    @FunctionProvider(AuxWorldProperties::class)
    override val properties: AuxWorldProperties
}

@Module
interface BetweenWorld : AuxWorld {
    // AuxWorld extra fields are properties of constructed ones
    @FunctionProvider(BetweenWorldProperties::class)
    override val properties: BetweenWorldProperties
}

// Z (inferred) properties of the schemas
open class ConWorldProperties(conWorld: ConWorld) {

    @Suppress("UNCHECKED_CAST")
    //LF @QST how to project this from Kazuki? If it was Map, would be as this
    val conAuthPurse: InjectiveMapping<Name, ConPurse> = as_InjectiveMapping((conWorld.purses as Mapping<Name, ConPurse>))
    //val conAuthPurse: InjectiveMapping<Name, AbPurse> = conWorld.purses.filterValues { it is ConPurse }.mapValues { it.value as ConPurse }
}

open class AuxWorldProperties(auxWorld: AuxWorld) : ConWorldProperties(auxWorld) {

}

class BetweenWorldProperties(betweenWorld: BetweenWorld) : AuxWorldProperties(betweenWorld) {

}

class ConWorldFunctions(conWorld: ConWorld) {}

//fun logbook(pds: Set<com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails>): Set<LogBook> =
//    (pds.map { pd ->  pd.td.fromPurse to pd } +
//        pds.map { pd -> pd.td.toPurse to pd })
//        .toSet().powerset()
