package com.anaplan.engineering.azuki.mondex.kazuki.betw

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.World
import com.anaplan.engineering.azuki.mondex.kazuki.isSubsetOf
import com.anaplan.engineering.azuki.mondex.kazuki.is_InjectiveMapping
import com.anaplan.engineering.azuki.mondex.kazuki.powerset
import com.anaplan.engineering.azuki.mondex.kazuki.property
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

    @FunctionProvider(ConWorldProperties::class)
    val properties: ConWorldProperties

    //LF @EK ConWorld doesn't have functions, given it is operated by BetweenWorld
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
    @FunctionProvider(BetweenWorldFunctions::class)
    val functions: BetweenWorldFunctions
}

// Z (inferred) properties of the schemas
open class ConWorldProperties(private val conWorld: ConWorld) {

    @Suppress("UNCHECKED_CAST")
    //LF @QST how to project this from Kazuki? If it was Map, would be as this
    val conAuthPurse by
        property(
            pre = { ->
                // only contain ConPurses
                forall(conWorld.purses.rng) { it -> it is ConPurse } &&
                // purses are injective on ConWorld
                is_InjectiveMapping(conWorld.purses)
            }
        )
        { as_InjectiveMapping((conWorld.purses as Mapping<Name, ConPurse>)) }
}

class AuxWorldProperties(auxWorld: AuxWorld) : ConWorldProperties(auxWorld) {

    // allLogs = archive + { (n, pd) | n in conAuthPurse.keys & pd in conAuthPurse[n].exLog }
    val allLogs by property {
        auxWorld.archive + as_Relation(auxWorld.properties.conAuthPurse.flatMap { (n, purse) -> purse.exLog.map { pd -> mk_(n, pd) } })
    }

    // Z has a set of all possible pay details where `pd.from` is known, not just those in the map!
    // so it can't simply be { pd | pd.from in conAuthPurse.keys }
    // authenticFrom = { pd | pd : PayDetails & pd.from in conAuthPurse.keys }
    //
    // For now this is a Sequence<PayDetails> (i.e. not a Kazuki type)
    val authenticFrom by property {
        allPayDetails(fromNames = auxWorld.properties.conAuthPurse.dom.asSequence())
    }

    val authenticTo by property {
        allPayDetails(toNames = auxWorld.properties.conAuthPurse.dom.asSequence())
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
            auxWorld.properties.conAuthPurse[pd.to].status == Status.epv &&
            (auxWorld.properties.conAuthPurse[pd.to].pdAuth == pd) }
    }

    val toInEpayee: Sequence<PayDetails> by property { TODO() }
    val fromInEpr : Sequence<PayDetails> by property { TODO() }
    val fromInEpa : Sequence<PayDetails> by property { TODO() }
}

class BetweenWorldFunctions(old: BetweenWorld) {

    fun xiBetweenWorld(before: BetweenWorld, after: BetweenWorld) =
        before.purses == after.purses &&
        before.ether == after.ether &&
        before.archive == after.archive

    val ignore = function(
        command = { name: Name -> mk_(old, Message.Bottom) },
        pre = { name -> true },
        post = { _, result ->
            val (dash, mbang) = result
            xiBetweenWorld(old, dash)
            mbang == Message.Bottom
        }
    )

    val phiBOp = function(
        command = { m: Message, name: Name, c: ConPurse ->
            mk_(old, c, m)
        },
        // ZEVES-PRG126 Table 8.3, p.87
        pre = { m, name, c ->
            m in old.ether &&
                name in old.properties.conAuthPurse.dom &&
                c == old.properties.conAuthPurse[name]
        },
        post = { m, name, c, result ->
            val (dash, cdash, mbang) = result
            //LF @EK assuming the * here is map overriding for a singleton maplet?
            dash.properties.conAuthPurse == old.properties.conAuthPurse * mk_(name, cdash) &&
            dash.archive == old.archive &&
            dash.ether == old.ether + { mbang }
        }
    )

    val abort = function(
        command = { name: Name ->
            TODO("Choose which path to take: ignore or abort")
            mk_(old, Message.Bottom)
        },
        pre = { name ->
            //LF @QST how to encode here the "or ignore.pre", which is just true?
            //        short circuiting will "kill" the check chain. Perhaps the VDM trick:
            //        change: "P and Q and R" to: "{P, Q, R} = {true}"?
            //
            //        That is, all preconditions must be valid, and the command has the choice
            //        to decide which path to take, so all true in this case
            val ignorePre = ignore.pre(name)
            val abortOkay = Message.Bottom in old.ether &&
                // In Z the \Theta ConPurse is mapped via phiBOp to the named conAuthPurse, so "fixing" it here too
                old.properties.conAuthPurse[name].functions.abortPurseOkay.pre(Message.Bottom) &&
                phiBOp.pre(Message.Bottom, name, old.properties.conAuthPurse[name])
            setOf(ignorePre, abortOkay).all { it } //== setOf(true)
        },
        post = { name, result ->
            val (dash, mbang) = result
            //LF @QST similar issue, even though both pres must be acceptable to begin with only
            //        one post will be true in some cases, given the implementation choice.
            //        so which one to check here?
            //
            //        Will check all, then have at least one true rather than all
            val ignoreOkay = ignore.post(name, result)
            val abortOkay = old.properties.conAuthPurse[name].functions.abortPurseOkay.post(
                Message.Bottom, mk_(dash.properties.conAuthPurse[name], mbang))
            setOf(ignoreOkay, abortOkay).any { it } &&
            mbang == Message.Bottom
        }
    )

    val startFrom = function(
        command = { name: Name ->
            mk_(old, Message.Bottom)
        },
        pre = { name ->
            true
        }
    )
}


//LF @QST is this the best/right way here? Or better to have powerset in Kazuki for KSet/Relation?
fun logbook(pds: Set<PayDetails>): Set<LogBook> =
    (pds.flatMap { pd -> listOf(mk_(pd.from, pd), mk_(pd.to, pd)) }.toSet())
        .powerset()
        .map(::as_Relation)
        .toSet()
