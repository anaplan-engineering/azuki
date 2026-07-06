package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook_Module.as_LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.azuki.examples.mondex.specification.isSubsetOf
import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.as_Relation
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.inter
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.plus
import com.anaplan.engineering.kazuki.core.property

@Module
interface AuxWorld : ConWorld {
    @FunctionProvider(AuxWorldProperties::class)
    val properties: AuxWorldProperties

    @Invariant
    fun noFurtherConstraints() =
        properties.definitelyLost + properties.maybeLost ==
            (properties.fromInEpa + properties.fromLogged) inter (properties.toInEpv + properties.toLogged)

    //TODO LF EAC - just an EAC to avoid over bearing checks
    //LF @QST should this (redundant check) be an invariant or another function?
    //    PRG126 5.2.1 p.43 not sure how (or if possible) to encode this
    //@Invariant
    fun noNewConstraints() = true //exists1(conAuthPurse) { (n, p) -> true }
}

class AuxWorldProperties(private val auxWorld: AuxWorld) {

    val totalBalance by property { auxWorld.conAuthPurse.sumOf { (_, p) -> p.balance } }

    //TODO this has to be `chosenLost subset of maybeLost` (e.g. a slice of losss)
    val chosenLost by property { maybeLost }

    //TODO LF I would like this to be an operator/property of AuxWorld?
    val lost = function(
        command = { name: Name ->
            (auxWorld.properties.definitelyLost union auxWorld.properties.chosenLost)
                .filter { pd -> pd.from == name }
                .sumOf { pd -> pd.value }
        },
        pre = { name -> name in auxWorld.conAuthPurse.dom },
        // Needed? Want to explicitly declare result as nat
        post = { name, result: nat -> true }
    )

    //TODO implement this via RAbCl? With chosen lost as slice of maybeLost?
    val totalLost by property {
        auxWorld.conAuthPurse.sumOf { (n, _) -> lost(n) }
    }

    val totalValue by property { totalBalance + totalLost }

    // allLogs = archive + { (n, pd) | n in conAuthPurse.keys & pd in conAuthPurse[n].exLog }
    //TODO LF  Relation.+ is broken here: transformSet { it.elements + m } nests a LogBook instead of flattening maplets.
    // Build one flat relation, then brand — avoids Relation.+ on LogBook.
    val allLogs by property {
        as_LogBook(
            as_Relation(
                auxWorld.archive.toList() + auxWorld.conAuthPurse.flatMap { (n, purse) ->
                    purse.exLog.map { pd -> mk_(n, pd) }
                }
            )
        )
    }

    //TODO LF SF discuss -
    // Given BetweenWorld's constraints depends on:
    //      * ether: B{1, 2, 3, 4, 9, 10, 11, 14, 15}
    //      * dom conAuthPurse: B{5, 6, 7, 8, 10, 12, 13, 16}
    //      * ignore open ended sets on PayDetails and stick to conAuthPurse contents only;
    // In Z:
    //      * the set of all possible pay details where `pd.from` is known, not just those in the map!
    //      * can't be { pd | pd.from in conAuthPurse.dom }
    //      * in VDM would be { pd | pd : PayDetails & pd.from in conAuthPurse.dom }
    val authenticFrom by property(
        //TODO LF SF - Narrows the sequence but would  make it finite? Say to size of conAuthPurse.dom?
        post = { r -> r.isSubsetOf { pd -> pd.from in auxWorld.conAuthPurse.dom } }
    ) {
//        val bound = auxWorld.conAuthPurse.dom.card.toInt()
//        as_Set(allPayDetailsFair(
//            bound = bound, fromNames = auxWorld.conAuthPurse.dom.asSequence())
//            .take(bound).toSet()
//        )
        // Using a conAuthPurse projection instead
        auxWorld.conAuthPurse.rng.filter { cp -> cp.pdAuth != null && cp.pdAuth!!.from == cp.name }
            .map { cp -> cp.pdAuth!! }.toSet()
    }

    val authenticTo by property(
        post = { r -> r.isSubsetOf { pd -> pd.to in auxWorld.conAuthPurse.dom } }
    ) {
//        val bound = auxWorld.conAuthPurse.dom.card.toInt()
//        as_Set(allPayDetailsFair(
//            bound = bound, toNames = auxWorld.conAuthPurse.dom.asSequence())
//            .take(bound).toSet()
//        )
        auxWorld.conAuthPurse.rng.filter { cp -> cp.pdAuth != null && cp.pdAuth!!.to == cp.name }
            .map { cp -> cp.pdAuth!! }.toSet()
    }

    val fromLogged by property(
        post = { r -> r.isSubsetOf { pd -> mk_(pd.from, pd) in auxWorld.properties.allLogs } }
    ) {
        as_Set(auxWorld.properties.authenticFrom
            .filter { pd -> mk_(pd.from, pd) in auxWorld.properties.allLogs })
    }

    val toLogged by property(
        post = { r -> r.isSubsetOf { pd -> mk_(pd.to, pd) in auxWorld.properties.allLogs } }
    ) {
        as_Set(auxWorld.properties.authenticTo
            .filter { pd -> mk_(pd.to, pd) in auxWorld.properties.allLogs })
    }

    //TODO LF - generalise these to a parameterised local function?
    val toInEpv by property(
        post = { r -> r.isSubsetOf { pd ->
            pd in auxWorld.properties.authenticTo &&
                auxWorld.conAuthPurse[pd.to].status == Status.epv &&
                auxWorld.conAuthPurse[pd.to].properties.pdAuth == pd
        } }
    ) {
        as_Set(auxWorld.properties.authenticTo
            .filter { pd ->
                auxWorld.conAuthPurse[pd.to].status == Status.epv &&
                    auxWorld.conAuthPurse[pd.to].properties.pdAuth == pd
            }
        )
    }

    val toInEpayee by property(
        post = { r -> r.isSubsetOf { pd ->
            pd in auxWorld.properties.authenticTo &&
                auxWorld.conAuthPurse[pd.to].status == Status.eaTo &&
                auxWorld.conAuthPurse[pd.to].properties.pdAuth == pd
        } }
    ) {
        as_Set(auxWorld.properties.authenticTo
            .filter { pd ->
                auxWorld.conAuthPurse[pd.to].status == Status.eaTo &&
                    auxWorld.conAuthPurse[pd.to].properties.pdAuth == pd
            }
        )
    }

    val fromInEpr by property(
        post = { r -> r.isSubsetOf { pd ->
            pd in auxWorld.properties.authenticFrom &&
                auxWorld.conAuthPurse[pd.from].status == Status.epr &&
                auxWorld.conAuthPurse[pd.from].properties.pdAuth == pd
        } }
    ) {
        as_Set(auxWorld.properties.authenticFrom
            .filter { pd ->
                auxWorld.conAuthPurse[pd.from].status == Status.epr &&
                    auxWorld.conAuthPurse[pd.from].properties.pdAuth == pd
            }
        )
    }

    val fromInEpa by property(
        post = { r -> r.isSubsetOf { pd ->
            pd in auxWorld.properties.authenticFrom &&
                auxWorld.conAuthPurse[pd.from].status == Status.epa &&
                auxWorld.conAuthPurse[pd.from].properties.pdAuth == pd
        } }
    ) {
        as_Set(auxWorld.properties.authenticFrom
            .filter { pd ->
                auxWorld.conAuthPurse[pd.from].status == Status.epa &&
                    auxWorld.conAuthPurse[pd.from].properties.pdAuth == pd
            }
        )
    }

    val definitelyLost by property {
        auxWorld.properties.toLogged inter (auxWorld.properties.fromLogged + fromInEpa)
    }

    val maybeLost by property {
        auxWorld.properties.toInEpv inter (auxWorld.properties.fromLogged + fromInEpa)
    }
}
