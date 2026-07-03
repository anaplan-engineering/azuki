package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.plus
import com.anaplan.engineering.kazuki.core.times

@Module
interface BetweenWorld : AuxWorld {
    @FunctionProvider(BetweenWorldFunctions::class)
    val functions: BetweenWorldFunctions
}



class BetweenWorldFunctions(private val old: BetweenWorld) {

    fun xiBetweenWorld(before: BetweenWorld, after: BetweenWorld) =
        before.conAuthPurse == after.conAuthPurse &&
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
                name in old.conAuthPurse.dom &&
                c == old.conAuthPurse[name]
        },
        post = { m, name, c, result ->
            val (dash, cdash, mbang) = result
            //LF @EK assuming the * here is map overriding for a singleton maplet?
            dash.conAuthPurse == old.conAuthPurse * mk_(name, cdash) &&
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
            val abortPre = Message.Bottom in old.ether &&
                // In Z the \Theta ConPurse is mapped via phiBOp to the named conAuthPurse, so "fixing" it here too
                old.conAuthPurse[name].functions.abortPurseOkay.pre(Message.Bottom) &&
                phiBOp.pre(Message.Bottom, name, old.conAuthPurse[name])
            setOf(ignorePre, abortPre).all { it } //== setOf(true)
        },
        //LF @QST these checks will be repeated.
        //        * their complexity is exposing the mechanics of promotion
        //        * promotion injects local updates into a (phiBop) constrained after state
        //        * e.g., local state=email; global state=mail server; promotion=inject sent email in server's state
        //        * ZEVES-PRG126 explicitly named the promotion disjunctions, PRG126 keeps them unnamed; below is like PRG126 (no name either)
        //          e.g. StartFromOkay = (\exists \Delta ConPurse & PhiBop \land StartFromPurseOkay)
        post = { name, result ->
            val (dash, mbang) = result
            // Initial message for Abort doesn't matter. Can be any
            val initialMsg = Message.Bottom
            //LF @QST similar issue, even though both pres must be acceptable to begin with only
            //        one post will be true in some cases, given the implementation choice.
            //        so which one to check here?
            //
            //        Will check all, then have at least one true rather than all
            val ignorePost = ignore.post(name, result)
            val (adash, abang) = old.conAuthPurse[name].functions.abortPurseOkay(initialMsg)
            val abortAfterSt = mk_(dash.conAuthPurse[name], mbang)
            val phiBopAfterSt = mk_(dash, abortAfterSt._1, abortAfterSt._2)
            val abortPost =
            // AbortPurseOkay operates on ConPurse:
            // * ConPurse' from result of abort (adash) compared with command's equivalent (abortAfterSt._1)
            // * Effect (resulting mk_(adash,abang)) is used in phiBop
            // * This injects resulting ConPurse' (adash) into the BetweenWorld' (dash).
            // * Check against the command's resulting BetweenWorld' after promotion (dash).
                // * Resulting message should be mbang, which should also be abang (they match).
                old.conAuthPurse[name].functions.abortPurseOkay.post(
                    initialMsg, abortAfterSt) &&
                    //LF @QST how can InteliJ know this is going to be the case (it isn't necessarily)?
                    //        Perhaps command is yet to be resolved?
                    abang == mbang &&
                    // PhiBop operates on BetweenWorld and a ConPurse:
                    // * ConPurse' from result of abort for promotion (adash)
                    // * BetweenWorld' from result of this function's command (dash)
                    // * Rest of after state comes from the ConPurse' in between world for given name
                    old.functions.phiBOp.post(initialMsg, name, adash, phiBopAfterSt)
            setOf(ignorePost, abortPost).any { it } &&
                mbang == Message.Bottom
        }
    )

    internal fun arbitraryUpConPurse(name: Name): ConPurse {
        val namedPurse = old.conAuthPurse[name]
        val arbitraryCPD = namedPurse.functions.arbitraryCPD()
        require(namedPurse.pdAuth != null) { "PDAuth must be non-null for ConPurse" }
        // This is a simplification/choice: the Z allows for a whole space of options from a new purse with these specific transforms
        return namedPurse.transform(
            nextSeqNo = namedPurse.nextSeqNo + 1U,
            pdAuth = namedPurse.pdAuth!!.transform(
                from = name, to = arbitraryCPD.name, value = arbitraryCPD.value,
                fromSeqNo = namedPurse.nextSeqNo, //TODO: or is this the transformed one... neeed to brush off the Z!
                toSeqNo = arbitraryCPD.nextSeqNo,
            ),
            status = Status.epr
        )
    }

    val startFrom = function(
        command = { name: Name ->
            TODO("Choice for ignore, abort, or startFromPurseOkay, which is only in ZEVES-PRG (see comment above)")
            mk_(old, Message.Bottom)
        },
        // ZEVES-PRG126 Table 8.3, p.87
        pre = { name ->
            // to avoid mixture of short circuiting x mistaken checks on vals depending on pdAuth!! Improve?
            if (old.conAuthPurse[name].pdAuth == null)
                false
            else {
                //(old.conAuthPurse[name].pdAuth != null) implies {
                // There will be some repeated checking here, hard to separate them apart easily without introducing mistakes?
                val abortPre = old.functions.abort.pre(name)
                val startMsg = Message.StartFrom(old.conAuthPurse[name].functions.arbitraryCPD())
                val purseName = old.conAuthPurse[name].name
                val startFromOkayPre =
                    phiBOp.pre(startMsg, name, old.conAuthPurse[name]) &&
                        old.conAuthPurse[name].functions.startFromPurseOkay.pre(startMsg) &&
                        Message.Bottom in old.ether &&
                        ((purseName in old.conAuthPurse.dom) implies { purseName != name }) &&
                        old.functions.arbitraryUpConPurse(purseName) !in old.conAuthPurse.rng
                startFromOkayPre
            }
        },
        post = { name, result ->
            val (dash, mbang) = result
            // Ignore \/ Abort is part of Abort, so can just reuse here to simplify promotion postconditions
            // On the other hand, because inner operations are total, the overall post will just be true
            //
            // This was another of the Mondex's manual proofs mistakes: getting away from proving by always taking
            // the ignore/abort paths! See ZEVES-PRG126 Chapter 8, which is most involved part of the exercise.
            val abortPost = old.functions.abort.post(name, result)
            val startFromPost =
                old.conAuthPurse[name].functions.startFromPurseOkay.post(
                    Message.StartFrom(old.conAuthPurse[name].functions.arbitraryCPD()),
                    mk_(dash.conAuthPurse[name], mbang))
            setOf(abortPost, startFromPost).any { it }
        }
    )
}
