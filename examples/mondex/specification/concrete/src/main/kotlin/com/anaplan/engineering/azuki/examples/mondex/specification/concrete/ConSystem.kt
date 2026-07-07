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
private infix fun ((ConWorld) -> ConWorld).then(g: (ConWorld) -> ConWorld): (ConWorld) -> ConWorld =
    { w -> g(this(w)) }

//TODO LF - with KSP could identify all elements in a VFunction command?
//          1) collect a update frame (e.g. variables that changed, to create xiRest post);
//          2) collect function calls (e.g. if some flag is on, chain pre/posts implicitly for stricter checking)

// Attempt at Kazuki function composition; needed some generalisation (e.g. param signature) / compartmentalisation (e.g. function providers)
private class ConWorldStep(
    val world: (ConSystemFunctions, TransferDetails) -> (ConWorld) -> ConWorld,
    val pre: (ConSystemFunctions, TransferDetails) -> Boolean,
    val post: (ConSystemFunctions, TransferDetails, ConSystem) -> Boolean
)

// Like Kazuki's function(...), but for host-parameterised world steps rather than VFunctions.
private fun worldStep(
    world: (ConSystemFunctions, TransferDetails) -> (ConWorld) -> ConWorld,
    pre: (ConSystemFunctions, TransferDetails) -> Boolean = { _, _ -> true },
    post: (ConSystemFunctions, TransferDetails, ConSystem) -> Boolean = { _, _, _ -> true }
): ConWorldStep = ConWorldStep(world, pre, post)

// Akin to Z \semi: P \semi Q \defs (\exists middle & P[middle/after] \land Q[middle/before])
private fun middleSystem(host: ConSystemFunctions, step: ConWorldStep, td: TransferDetails): ConSystem =
    mk_ConSystem(step.world(host, td)(host.system.conWorld))

// (f ; g) at step level: chains world/pre/post before materialising to a VFunction.
private infix fun ConWorldStep.compose(g: ConWorldStep): ConWorldStep = worldStep(
    world = { host, td -> world(host, td) then g.world(host, td) },
    pre = { host, td ->
        pre(host, td)
            && g.pre(ConSystemFunctions(middleSystem(host, this, td)), td)
    },
    post = { host, td, result ->
        val middle = middleSystem(host, this, td)
        post(host, td, middle) && g.post(ConSystemFunctions(middle), td, result)
    },
)

class ConSystemFunctions(val system: ConSystem) {

    // ConWorld delegated receiver to perform world updates functionally in monadic style
    fun update(update: (ConWorld) -> ConWorld) = updateSystem(update(system.conWorld))

    private val updateSystem = function(
        command = { after: ConWorld -> mk_ConSystem(after) },
        // pre = { after -> after != system.conWorld },  // this is allowed
    )

    // Materialise a step as a Kazuki VFunction bound to this provider's before-state.
    private fun worldFunction(step: ConWorldStep): VFunction1<TransferDetails, ConSystem> = function(
        command = { td -> update(step.world(this, td)) },
        pre = { td -> step.pre(this, td) },
        post = { td, dash -> step.post(this, td, dash) },
    )

    // Initial message creation for given transfer details
    private fun startTransferMessages(td: TransferDetails): Pair<Message.StartFrom, Message.StartTo> {
        val fromPurse = system.conWorld.conAuthPurse[td.from]
        val toPurse = system.conWorld.conAuthPurse[td.to]
        // StartFrom message gets the toPurse's name and seq no;
        return Message.StartFrom(
            mk_CounterPartyDetails(
                toPurse.name,
                td.value,
                toPurse.nextSeqNo,
            ),
        // StartTo message gets the fromPurse's name and seq no;
        ) to Message.StartTo(
            mk_CounterPartyDetails(
                fromPurse.name,
                td.value,
                fromPurse.nextSeqNo,
            ),
        )
    }

    // PRG126 Sect. 4.8 Invisible Operations: Increase + Abort
    // * abort in particular talks about the ConPurse pdAuth being undefined - hence why we left it as nullable in ConPurse PayDetails?
    // * Protocol plays abort at the beginning of a transfer, and abort is innocous
    // * Protocol plays abort at epr/epv/epa and it generates a log
    //   - PRG126 Sect. 2.3.1 Security Property 2.2: LogIfNecessary
    //   - PRG126 Sect. 4.6 ConPurse invariants + 4.8.2 AbortPurseOkay pre
    //   - we need to establish here the validity of purses pdAuth (see abortPurseOkay.pre comments)
    private val establishValidPayDetailsStep = worldStep(
        world = { _, td ->
            { w ->
                val fromPurse = w.conAuthPurse[td.from]
                val toPurse = w.conAuthPurse[td.to]

                // see PRG126, p.31 \mu-expr in StartFromPurseEafromOkay
                val fromPurseDash = fromPurse.transform(
                    pdAuth = mk_PayDetails(
                        from      = fromPurse.name, // same as td.from,
                        to        = td.to,
                        value     = td.value,
                        fromSeqNo = fromPurse.nextSeqNo,
                        toSeqNo   = toPurse.nextSeqNo,
                    ),
                )
                // see see PRG126, p.32 \mu-expr in StartToPurseEafromOkay
                val toPurseDash = toPurse.transform(
                    pdAuth = mk_PayDetails(
                        from      = td.to,
                        to        = td.from,
                        value     = td.value,
                        fromSeqNo = toPurse.nextSeqNo,
                        toSeqNo   = fromPurse.nextSeqNo,
                    ),
                )
                w.transform(
                    conAuthPurse = w.conAuthPurse * mk_InjectiveMapping(
                        mk_(td.from, fromPurseDash),
                        mk_(td.to, toPurseDash),
                    ),
                )
            }
        },
        pre = { host, td ->
            td.from in host.system.conWorld.conAuthPurse.dom &&
                td.to in host.system.conWorld.conAuthPurse.dom &&
                host.system.conWorld.conAuthPurse[td.from].pdAuth == null &&
                host.system.conWorld.conAuthPurse[td.to].pdAuth == null
        },
        post = { host, td, dash ->
            // Explicitly check this does not breaks the abstract world's expectations!
            dash.conWorld.conAuthPurse.domSubtract(mk_Set(td.from, td.to)) ==
                host.system.conWorld.conAuthPurse.domSubtract(mk_Set(td.from, td.to)) &&
                dash.conWorld.conAuthPurse[td.from].pdAuth != null &&
                dash.conWorld.conAuthPurse[td.to].pdAuth != null
        },
    )

    private val startFromToOnWorldStep = worldStep(
        world = { _, td ->
            { w ->
                val fromPurse = w.conAuthPurse[td.from]
                val toPurse = w.conAuthPurse[td.to]
                val (fromMsg, toMsg) = ConSystemFunctions(mk_ConSystem(w)).startTransferMessages(td)

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
        },
        pre = { host, td ->
            val (fromMsg, toMsg) = host.startTransferMessages(td)
            val fromPurse = host.system.conWorld.conAuthPurse[td.from]
            val toPurse = host.system.conWorld.conAuthPurse[td.to]
            fromPurse.functions.startFromPurseOkay.pre(fromMsg) &&
                toPurse.functions.startToPurseOkay.pre(toMsg)
        },
        post = { host, td, dash ->
            val w = host.system.conWorld
            val fromPurse = w.conAuthPurse[td.from]
            val toPurse = w.conAuthPurse[td.to]
            val (fromMsg, toMsg) = host.startTransferMessages(td)
            val resultFrom = fromPurse.functions.startFromPurseOkay(fromMsg)
            val resultTo = toPurse.functions.startToPurseOkay(toMsg)
            fromPurse.functions.startFromPurseOkay.post(fromMsg, resultFrom) &&
                toPurse.functions.startToPurseOkay.post(toMsg, resultTo) &&
                dash.conWorld.conAuthPurse[td.from] == resultFrom._1 &&
                dash.conWorld.conAuthPurse[td.to] == resultTo._2 &&
                dash.conWorld.ether == w.ether + resultFrom._2 + resultTo._2
        },
    )

    val establishValidPayDetails = worldFunction(establishValidPayDetailsStep)

    val startFromToOnWorld = worldFunction(startFromToOnWorldStep)

    // PRG126 Sect. 5.9 The Complete Protocol: informally as "StartFrom \semi StartTo \semi Req \semi Val \semi Ack"
    // * Note that "Other operations may be interleaved in an actual transfer"
    // PRG126 Sect. 4.8 Invisible Operations: Increase + Abort
    // * abort in particular talks about the ConPurse pdAuth being undefined - hence why we left it as nullable in ConPurse PayDetails?
    // * Protocol plays abort at the beginning of a transfer, and abort is innocous
    // * Protocol plays abort at epr/epv/epa and it generates a log
    //   - PRG126 Sect. 2.3.1 Security Property 2.2: LogIfNecessary
    //   - PRG126 Sect. 4.6 ConPurse invariants + 4.8.2 AbortPurseOkay pre
    //
    // startTransfer \defs establishValidPayDetails \semi startFromToOnWorld
    val startTransfer = worldFunction(establishValidPayDetailsStep compose startFromToOnWorldStep)

}
