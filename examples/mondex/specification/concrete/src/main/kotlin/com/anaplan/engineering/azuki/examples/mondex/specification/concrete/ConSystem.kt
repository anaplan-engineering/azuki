package com.anaplan.engineering.azuki.examples.mondex.specification.concrete

import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.as_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.is_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.mk_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.mk_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.WorldStep
import com.anaplan.engineering.azuki.examples.mondex.specification.WorldStepFrameStack
import com.anaplan.engineering.azuki.examples.mondex.specification.WorldStepHostContext
import com.anaplan.engineering.azuki.examples.mondex.specification.WorldSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.WorldSystemFunctions
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Bottom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.composeWith
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConSystem_Module.mk_ConSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.then
import com.anaplan.engineering.azuki.examples.mondex.specification.worldCompose
import com.anaplan.engineering.azuki.examples.mondex.specification.worldStep
import com.anaplan.engineering.kazuki.core.*

@Module
interface ConSystem: WorldSystem {

    override val world: ConWorld

    @FunctionProvider(ConSystemFunctions::class)
    override val functions: ConSystemFunctions
}

typealias ConWorldMonad = (ConWorld) -> ConWorld
// World forward composition: (f ; g)(W) = g(f(W)), shame `;` not possible
infix fun <W> ((W) -> W).then(next: (W) -> W): (W) -> W = { w -> next(this(w)) }

class ConSystemFunctions(val system: ConSystem) : WorldSystemFunctions() {


    // ConWorld delegated receiver to perform world updates functionally in monadic style
    fun update(modify: (ConWorld) -> ConWorld) = updateSystem(modify(system.world))

    fun updateSteps(vararg steps: ConWorldMonad) =
        update(steps.reduce { acc, step -> acc then step })

    private val updateSystem = function(
        command = { after: ConWorld -> mk_ConSystem(after) },
        // pre = { after -> after != system.conWorld },  // this is allowed
    )

    val startTransfer = function(
        command = { pd: PayDetails ->
            updateSteps(
                { w ->
                    val (from, to) = startTransferMessages(pd)
                    w.transform(ether = mk_Set())
                },
                { w ->
                    w.transform(ether = w.ether + mk_Set(Bottom))
                },
            )
        },
        pre = { _ -> true },
        post = { _, _ -> true },
    )

    // Initial message creation for given transfer details
    private fun startTransferMessages(td: TransferDetails): Pair<StartFrom, StartTo> {
        val fromPurse = system.world.conAuthPurse[td.from]
        val toPurse = system.world.conAuthPurse[td.to]
        // StartFrom message gets the toPurse's name and seq no;
        return mk_StartFrom(
            mk_CounterPartyDetails(
                toPurse.name,
                td.value,
                toPurse.nextSeqNo,
            ),
        // StartTo message gets the fromPurse's name and seq no;
        ) to mk_StartTo(
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
    //
    // TODO we are trying to model here the BetweenInitState, which will include the initial ether with partially started messages for startFrom and startTo!
    private val establishValidPayDetailsFromStep = conWorldStep(
        command = { _, m ->
            { w ->
                val cpd = as_StartFrom(m).cpd
                val wrongNameBang = cpd.name
                val fromPurse = w.conAuthPurse[wrongNameBang]
                // see PRG126, p.31 \mu-expr in StartFromPurseEafromOkay
                // the cpd is the `to` purse of interest
                val fromPurseDash = fromPurse.transform(
                    pdAuth = mk_PayDetails(
                        from      = wrongNameBang,
                        to        = cpd.name,
                        value     = cpd.value,
                        fromSeqNo = fromPurse.nextSeqNo,
                        toSeqNo   = cpd.nextSeqNo,
                    ),
                )

                w.transform(
                    conAuthPurse = w.conAuthPurse * mk_InjectiveMapping(
                        mk_(wrongNameBang, fromPurseDash)
                    ),
                )
            }
        },
        pre = { before, m ->
            // TODO LF - this needs to have somehow access to a purse so that validStartFrom.pre can be called
            //           in Z, this gets "composed" in place when the promotion is put together!
            is_StartFrom(m)
                &&
                as_StartFrom(m).cpd.let { cpd ->
                    cpd.name in before.system.world.conAuthPurse.dom &&
                    before.system.world.conAuthPurse[cpd.name].pdAuth == null
                }
        },
        post = { before, m, dash ->
            // Explicitly check this does not breaks the abstract world's expectations!
            as_StartFrom(m).cpd.let { cpd ->
                dash.world.conAuthPurse.domSubtract(mk_Set(cpd.name)) ==
                    before.system.world.conAuthPurse.domSubtract(mk_Set(cpd.name)) &&
                    dash.world.conAuthPurse[cpd.name].pdAuth != null
            }
        },
    )

//    private val establishValidPayDetailsToStep = worldStep(
//        step = { _, m ->
//            { w ->
//                // see see PRG126, p.32 \mu-expr in StartToPurseEafromOkay
//                // the cpd is the `from` purse of interest
//                val cpd = as_StartFrom(m).cpd
//                val toPurse = w.conAuthPurse[cpd.name]
//                val toPurseDash = toPurse.transform(
//                    pdAuth = mk_PayDetails(
//                        from      = cpd.name,
//                        to        = toPurse.name,
//                        value     = cpd.value,
//                        fromSeqNo = cpd.nextSeqNo,
//                        toSeqNo   = toPurse.nextSeqNo,
//                    ),
//                )
//                w.transform(
//                    conAuthPurse = w.conAuthPurse * mk_InjectiveMapping(
//                        mk_(td.from, fromPurseDash),
//                        mk_(td.to, toPurseDash),
//                    ),
//                )
//            }
//        },
//        pre = { host, td ->
//            td.from in host.system.conWorld.conAuthPurse.dom &&
//                td.to in host.system.conWorld.conAuthPurse.dom &&
//                host.system.conWorld.conAuthPurse[td.from].pdAuth == null &&
//                host.system.conWorld.conAuthPurse[td.to].pdAuth == null
//        },
//        post = { host, td, dash ->
//            // Explicitly check this does not breaks the abstract world's expectations!
//            dash.conWorld.conAuthPurse.domSubtract(mk_Set(td.from, td.to)) ==
//                host.system.conWorld.conAuthPurse.domSubtract(mk_Set(td.from, td.to)) &&
//                dash.conWorld.conAuthPurse[td.from].pdAuth != null &&
//                dash.conWorld.conAuthPurse[td.to].pdAuth != null
//        },
//    )

    // From step CPD relates with TO purse
    private val startFromOnWorldStep = conWorldStep(
        command = { _, _ ->
            { w ->
//                val fromPurse = w.conAuthPurse[td.from]
//                val toPurse = w.conAuthPurse[td.to]
//                val (fromMsg, toMsg) = ConSystemFunctions(mk_ConSystem(w)).startTransferMessages(td)
//
//                val resultFrom = fromPurse.functions.startFromPurseOkay(fromMsg)
//                val resultTo = toPurse.functions.startToPurseOkay(toMsg)
//
//                w.transform(
//                    conAuthPurse = w.conAuthPurse * mk_InjectiveMapping(
//                        mk_(td.from, resultFrom._1),
//                        mk_(td.to, resultTo._2),
//                    ),
//                    // TODO - certainly not sure where the messages go!
//                    ether = w.ether + resultFrom._2 + resultTo._2,
//                )
                w
            }
        },
        pre = { _, _ ->
//            val (fromMsg, toMsg) = host.startTransferMessages(td)
//            val fromPurse = host.system.conWorld.conAuthPurse[td.from]
//            val toPurse = host.system.conWorld.conAuthPurse[td.to]
//            fromPurse.functions.startFromPurseOkay.pre(fromMsg) &&
//                toPurse.functions.startToPurseOkay.pre(toMsg)
            true
        },
        post = { _, _, _ ->
//            val w = host.system.conWorld
//            val fromPurse = w.conAuthPurse[td.from]
//            val toPurse = w.conAuthPurse[td.to]
//            val (fromMsg, toMsg) = host.startTransferMessages(td)
//            val resultFrom = fromPurse.functions.startFromPurseOkay(fromMsg)
//            val resultTo = toPurse.functions.startToPurseOkay(toMsg)
//            fromPurse.functions.startFromPurseOkay.post(fromMsg, resultFrom) &&
//                toPurse.functions.startToPurseOkay.post(toMsg, resultTo) &&
//                dash.conWorld.conAuthPurse[td.from] == resultFrom._1 &&
//                dash.conWorld.conAuthPurse[td.to] == resultTo._2 &&
//                dash.conWorld.ether == w.ether + resultFrom._2 + resultTo._2
            true
        },
    )

    // Materialise a step as a Kazuki VFunction bound to this provider's before-state.
    val startTransferFrom2 = worldCompose(
        establishValidPayDetailsFromStep compose startFromOnWorldStep,
        conSystemWorldContext,
    ) { transform ->
        update(transform)
    }
    //val startTransferTo   = worldCompose(..., conSystemWorldContext) { transform -> update(transform) }

    val worldStepFrames = WorldStepFrameStack<ConSystem>()
}


// Set up the composition step participants
private typealias ConWorldStep = WorldStep<ConSystem, ConWorld, ConSystemFunctions, Message>

// Identify how to expose the right parts in context
private val conSystemWorldContext = WorldStepHostContext(
    worldOfHost = { it.system.world },
    systemOfWorld = ::mk_ConSystem,
    functionsOfSystem = ::ConSystemFunctions,
    frameStackOfHost = { it.worldStepFrames },
)

// (f ; g) here will always use the same context
private infix fun ConWorldStep.compose(g: ConWorldStep) = composeWith(g, conSystemWorldContext)

private fun conWorldStep(
    command: (ConSystemFunctions, Message) -> (ConWorld) -> ConWorld,
    pre: (ConSystemFunctions, Message) -> Boolean = { _, _ -> true },
    post: (ConSystemFunctions, Message, ConSystem) -> Boolean = { _, _, _ -> true },
): ConWorldStep = worldStep(command, pre, post)

