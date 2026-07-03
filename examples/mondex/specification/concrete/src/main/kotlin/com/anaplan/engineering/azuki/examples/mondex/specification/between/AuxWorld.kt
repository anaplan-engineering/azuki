package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.azuki.examples.mondex.specification.isSubsetOf
import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.as_Relation
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.plus
import com.anaplan.engineering.kazuki.core.property

@Module
interface AuxWorld : ConWorld {
    // AuxWorld extra fields are properties of constructed ones
    //LF @QST can I do this here? want to extend the world's properties
    @FunctionProvider(AuxWorldProperties::class)
    val properties: AuxWorldProperties

    //LF @QST should this (redundant check) be an invariant or another function?
    //@Invariant
    //fun noNewConstraints(): Boolean = {
    // PRG126 5.2.1 p.43 . not sure how (or if possible) to encode this
    //    val newVariables = exists(....)
    //}
}

class AuxWorldProperties(private val auxWorld: AuxWorld) {

    // allLogs = archive + { (n, pd) | n in conAuthPurse.keys & pd in conAuthPurse[n].exLog }
    val allLogs by property {
        auxWorld.archive + as_Relation(auxWorld.conAuthPurse.flatMap { (n, purse) ->
            purse.exLog.map { pd ->
                mk_(n,
                    pd)
            }
        })
    }

    // Z has a set of all possible pay details where `pd.from` is known, not just those in the map!
    // so it can't simply be { pd | pd.from in conAuthPurse.keys }
    // authenticFrom = { pd | pd : PayDetails & pd.from in conAuthPurse.keys }
    //
    // For now this is a Sequence<PayDetails> (i.e. not a Kazuki type)
    val authenticFrom by property {
        allPayDetails(fromNames = auxWorld.conAuthPurse.dom.asSequence())
    }

    val authenticTo by property {
        allPayDetails(toNames = auxWorld.conAuthPurse.dom.asSequence())
    }

    val fromLogged by property(
        //LF @EK this narrows the sequence but wouldn't make it finite
        post = { r -> r.isSubsetOf { pd -> mk_(pd.from, pd) in auxWorld.properties.allLogs } }
    ) {
        auxWorld.properties.authenticFrom.filter { pd -> mk_(pd.from, pd) in auxWorld.properties.allLogs }
    }

    val toLogged by property {
        auxWorld.properties.authenticTo.filter { pd -> mk_(pd.to, pd) in auxWorld.properties.allLogs }
    }

    val toInEpv by property {
        auxWorld.properties.authenticTo.filter { pd ->
            auxWorld.conAuthPurse[pd.to].status == Status.epv &&
                (auxWorld.conAuthPurse[pd.to].pdAuth == pd)
        }
    }

    val toInEpayee: com.anaplan.engineering.kazuki.core.Sequence<PayDetails> by property { TODO() }
    val fromInEpr: com.anaplan.engineering.kazuki.core.Sequence<PayDetails> by property { TODO() }
    val fromInEpa: Sequence<PayDetails> by property { TODO() }
}
