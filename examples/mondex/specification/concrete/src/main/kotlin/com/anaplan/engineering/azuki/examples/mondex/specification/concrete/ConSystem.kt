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

// World forward composition on transforms: (f ; g)(W) = g(f(W))
private infix fun ((ConWorld) -> ConWorld).then(g: (ConWorld) -> ConWorld): (ConWorld) -> ConWorld =
    { w -> g(this(w)) }

//TODO LF - with KSP could identify all elements in a VFunction command?
//          1) collect a update frame (e.g. variables that changed, to create xiRest post);
//          2) collect function calls (e.g. if some flag is on, chain pre/posts implicitly for stricter checking)
//
//TODO: Attempt at Kazuki function composition; needed some generalisation (e.g. param signature) / compartmentalisation (e.g. function providers)
private class ConWorldStep(
    val world: (ConSystemFunctions, TransferDetails) -> (ConWorld) -> ConWorld,
    val pre: (ConSystemFunctions, TransferDetails) -> Boolean,
    val post: (ConSystemFunctions, TransferDetails, ConSystem) -> Boolean
)

class ConSystemFunctions(val system: ConSystem) {

    // ConWorld delegated receiver to perform world updates functionally in monadic style
    fun update(update: (ConWorld) -> ConWorld) = updateSystem(update(system.conWorld))

    private val updateSystem = function(
        command = { after: ConWorld -> mk_ConSystem(after) },
        // pre = { after -> after != system.conWorld },  // this is allowed
    )

    private fun worldFunction(step: ConWorldStep): VFunction1<TransferDetails, ConSystem> = function(
        command = { td -> update(step.world(this, td)) },
        pre = { td -> step.pre(this, td) },
        post = { td, dash -> step.post(this, td, dash) },
    )

    // (f ; g): command chains world updates; pre/post follow Z sequential composition.
    private infix fun ConWorldStep.fwdCompose(g: ConWorldStep): VFunction1<TransferDetails, ConSystem> {
        val host = this@ConSystemFunctions
        return function(
            command = { td ->
                update(world(host, td) then g.world(host, td))
            },
            pre = { td ->
                pre(host, td) && ConSystemFunctions(mk_ConSystem(world(host, td)(host.system.conWorld))).let { middle ->
                    g.pre(middle, td)
                }
            },
            post = { td, result ->
                val middle = mk_ConSystem(world(host, td)(host.system.conWorld))
                post(host, td, middle) && ConSystemFunctions(middle).let { mid ->
                    g.post(mid, td, result)
                }
            },
        )
    }

    private fun startTransferMessages(td: TransferDetails): Pair<Message.StartFrom, Message.StartTo> {
        val fromPurse = system.conWorld.conAuthPurse[td.from]
        val toPurse = system.conWorld.conAuthPurse[td.to]
        return Message.StartFrom(
            mk_CounterPartyDetails(
                toPurse.name,
                td.value,
                toPurse.nextSeqNo,
            ),
        ) to Message.StartTo(
            mk_CounterPartyDetails(
                fromPurse.name,
                td.value,
                fromPurse.nextSeqNo,
            ),
        )
    }

    private val establishValidPayDetailsStep = ConWorldStep(
        world = { _, td ->
            { w ->
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

    private val startFromToOnWorldStep = ConWorldStep(
        world = { host, td ->
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

    // PRG126 Sect. 4.8 Invisible Operations: Increase + Abort
    // * abort in particular talks about the ConPurse pdAuth being undefined - hence why we left it as nullable in ConPurse PayDetails?
    // * Protocol plays abort at the beginning of a transfer, and abort is innocous
    // * Protocol plays abort at epr/epv/epa and it generates a log
    //   - PRG126 Sect. 2.3.1 Security Property 2.2: LogIfNecessary
    //   - PRG126 Sect. 4.6 ConPurse invariants + 4.8.2 AbortPurseOkay pre
    //   - we need to establish here the validity of purses pdAuth (see abortPurseOkay.pre comments)
    val establishValidPayDetails = worldFunction(establishValidPayDetailsStep)

    // PRG126 Sect. 5.9 The Complete Protocol: first two steps of "StartFrom \semi StartTo \semi Req \semi Val \semi Ack"
    val startFromToOnWorld = worldFunction(startFromToOnWorldStep)

    // establishValidPayDetails ; startFromToOnWorld
    val startTransfer = establishValidPayDetailsStep fwdCompose startFromToOnWorldStep

}
