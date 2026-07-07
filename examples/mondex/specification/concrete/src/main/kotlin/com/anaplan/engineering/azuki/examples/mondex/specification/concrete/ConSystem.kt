package com.anaplan.engineering.azuki.examples.mondex.specification.concrete

import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConSystem_Module.component1
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConSystem_Module.mk_ConSystem
import com.anaplan.engineering.kazuki.core.*

@Module
interface ConSystem {

    val conWorld: ConWorld

    @FunctionProvider(ConSystemFunctions::class)
    val functions: ConSystemFunctions
}

// World forward composition: (f ; g)(W) = g(f(W))
//TODO Tried inside ConSystemFunctions but Kotlin wasn't happy with my infix params. So had it with establishValidPayDetailsOnWorld
private infix fun ((ConWorld) -> ConWorld).then(g: (ConWorld) -> ConWorld): (ConWorld) -> ConWorld =
    { w -> g(this(w)) }

class ConSystemFunctions(val system: ConSystem) {

    // ConWorld delegated receiver to perform world updates functionally in monadic style
    fun update(update: (ConWorld) -> ConWorld) = updateSystem(update(system.conWorld))

    private val updateSystem = function(
        command = { after: ConWorld -> mk_ConSystem(after) },
        // pre = { after -> after != system.conWorld },  // this is allowed
    )

    private fun establishValidPayDetailsOnWorld(td: TransferDetails): (ConWorld) -> ConWorld = { w ->
        val fromPurse = w.conAuthPurse[td.from]
        val toPurse = w.conAuthPurse[td.to]

        val fromPurseDash = fromPurse.transform(
            pdAuth = mk_PayDetails(
                td.from, td.to, td.value,
                fromPurse.nextSeqNo,
                toPurse.nextSeqNo,
            ),
        )
        val toPurseDash = toPurse.transform(
            pdAuth = mk_PayDetails(
                td.to, td.from, td.value,
                toPurse.nextSeqNo, fromPurse.nextSeqNo,
            ),
        )
        w.transform(
            conAuthPurse = w.conAuthPurse * mk_InjectiveMapping(
                mk_(td.from, fromPurseDash),
                mk_(td.to, toPurseDash),
            ),
        )
    }

    private fun startFromToOnWorld(td: TransferDetails): (ConWorld) -> ConWorld = { w ->
        val fromPurse = w.conAuthPurse[td.from]
        val toPurse = w.conAuthPurse[td.to]

        val fromMsg = Message.StartFrom(
            mk_CounterPartyDetails(
                toPurse.name,
                td.value,
                toPurse.nextSeqNo,
            ),
        )
        val toMsg = Message.StartTo(
            mk_CounterPartyDetails(
                fromPurse.name,
                td.value,
                fromPurse.nextSeqNo,
            ),
        )

        val resultFrom = fromPurse.functions.startFromPurseOkay(fromMsg)
        val resultTo = toPurse.functions.startToPurseOkay(toMsg)

        w.transform(
            conAuthPurse = w.conAuthPurse * mk_InjectiveMapping(
                mk_(td.from, resultFrom._1),
                mk_(td.to, resultTo._2),
            ),
            // TODO - certainly not sure where the messages go!
            ether = w.ether + resultFrom._2 + resultTo._2,
        )
    }

    // PRG126 Sect. 4.8 Invisible Operations: Increase + Abort
    // * abort in particular talks about the ConPurse pdAuth being undefined - hence why we left it as nullable in ConPurse PayDetails?
    // * Protocol plays abort at the beginning of a transfer, and abort is innocous
    // * Protocol plays abort at epr/epv/epa and it generates a log
    //   - PRG126 Sect. 2.3.1 Security Property 2.2: LogIfNecessary
    //   - PRG126 Sect. 4.6 ConPurse invariants + 4.8.2 AbortPurseOkay pre
    //   - we need to establish here the validity of purses pdAuth (see abortPurseOkay.pre comments)
    val establishValidPayDetails = function(
        command = { td: TransferDetails ->
            update(establishValidPayDetailsOnWorld(td))
        },
        // pdAuth is indeed underfined at this point
        pre = { td ->
            td.from in system.conWorld.conAuthPurse.dom &&
                td.to in system.conWorld.conAuthPurse.dom &&
                system.conWorld.conAuthPurse[td.from].pdAuth == null &&
                system.conWorld.conAuthPurse[td.to].pdAuth == null
        },
        post = { td, dash ->
            // Explicitly check this does not breaks the abstract world's expectations!
            dash.conWorld.conAuthPurse.domSubtract(mk_Set(td.from, td.to)) ==
                system.conWorld.conAuthPurse.domSubtract(mk_Set(td.from, td.to)) &&
                // Check valid pay details have been established
                dash.conWorld.conAuthPurse[td.from].pdAuth != null &&
                dash.conWorld.conAuthPurse[td.to].pdAuth != null
        }
    )

    // PRG126 Sect. 5.9 The Complete Protocol: first two steps of "StartFrom \semi StartTo \semi Req \semi Val \semi Ack"

    //TODO LF - with KSP could identify all elements in a VFunction command?
    //          1) collect a update frame (e.g. variables that changed, to create xiRest post);
    //          2) collect function calls (e.g. if some flag is on, chain pre/posts implicitly for stricter checking)
    //
    //TODO LF/SF - this kind of composition within Kazuki as pre/posts chains would be nice too for drudegery of checking chained pre/posts?
    //          f = function(...)
    //          g = function(...)
    //          (f ; g) = function(command = {...}, pre = { f.pre && g.pre(f.post) }, post = { g.post } ...
    //          Attempted a forward composition operator here; perhaps generalise
    val startTransfer = function(
        command = { td: TransferDetails ->
            update(establishValidPayDetailsOnWorld(td) then startFromToOnWorld(td))
        },
        pre = { td -> establishValidPayDetails.pre(td) },
 //       post = { td, dash -> startFromToOnWorld.post(td, dash) }
    )

}
