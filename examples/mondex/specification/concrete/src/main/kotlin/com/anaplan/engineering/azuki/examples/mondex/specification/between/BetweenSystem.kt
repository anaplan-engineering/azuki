package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.mk_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem_Module.mk_BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CPDUnprotectedMessage_Module.as_CPDUnprotectedMessage
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.mk_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.mk_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.mk_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.mk_Val
import com.anaplan.engineering.azuki.examples.mondex.specification.mondexPretty
import com.anaplan.engineering.azuki.examples.mondex.specification.then
import com.anaplan.engineering.kazuki.core.*

@Module
interface BetweenSystem {

    val world: BetweenWorld
    //val last: Message

    @FunctionProvider(BetweenSystemFunctions::class)
    val functions: BetweenSystemFunctions
}

typealias BetweenWorldMonad = (BetweenWorld) -> BetweenWorld
// World forward composition: (f ; g)(W) = g(f(W)), shame `;` not possible
infix fun <W> ((W) -> W).then(next: (W) -> W): (W) -> W = { w -> next(this(w)) }


class BetweenSystemFunctions(val system: BetweenSystem) {
    // BetweenWorld delegated receiver to perform world updates functionally in monadic style
    fun update(modify: BetweenWorldMonad) =
        updateSystem(modify(system.world))

    fun updateSteps(vararg steps: BetweenWorldMonad) =
        update(steps.reduce { acc, step -> acc then step })

    private val updateSystem = function(
        command = { after: BetweenWorld -> mk_BetweenSystem(after) },
        // pre = { after -> after != system.BetweenWorld },  // this is allowed
    )

    //TODO LF any more elegant way of doing this?
    inline fun <reified T : Message> getMessage(pd: PayDetails): T {
        val fromPurse = system.world.conAuthPurse[pd.from]
        val toPurse = system.world.conAuthPurse[pd.to]

        @Suppress("UNCHECKED_CAST")
        return when (T::class) {
            StartFrom::class -> mk_StartFrom(
                mk_CounterPartyDetails(toPurse.name, pd.value, toPurse.nextSeqNo)
            ) as T
            StartTo::class -> mk_StartTo(
                mk_CounterPartyDetails(fromPurse.name, pd.value, fromPurse.nextSeqNo)
            ) as T
            Req::class -> mk_Req(pd) as T
            Val::class -> mk_Val(pd) as T
            Ack::class -> mk_Ack(pd) as T
            else -> error("Unsupported message type: ${T::class.simpleName}")
        }
    }

    // PRG126 Sect. 4.8 Invisible Operations: Increase + Abort
    // * abort talks about the ConPurse pdAuth being undefined - hence why we left it as nullable in ConPurse PayDetails?
    // * Protocol plays abort at the beginning of a transfer, and abort is innocuous
    // * Protocol plays abort at epr/epv/epa and it generates a log
    //   - PRG126 Sect. 2.3.1 Security Property 2.2: LogIfNecessary
    //   - PRG126 Sect. 4.6 ConPurse invariants + 4.8.2 AbortPurseOkay pre
    //   - we need to establish here the validity of purses pdAuth (see abortPurseOkay.pre comments)
    val startTransfer = function(
        command = { pd: PayDetails ->

            // We are trying to model here the BetweenInitState, which will include
            // the initial ether with partially started messages for startFrom and startTo.
            val mFrom = getMessage<StartFrom>(pd)
            val mTo = getMessage<StartTo>(pd)

            //@4paper - Explicitly changing the ether is dangerous, but the invariants of BetweenWorld will enforce correctness
            //          We need to do that explicitly, given we don't have infinite paydetails for BetweenInitState
            val system1 = update { w -> w.transform(ether = w.ether + mk_Set(mFrom, mTo)) }

            //TODO LF SF - inner specification of these not being checked; Would be if we had a compose operator
            val system2 = system1.functions.update { w ->
                println("1: Create transfer from=${pd.from}, pd=${pd.mondexPretty()}")
                println("1.1: w=${w.mondexPretty()}")
                println("1.2: StartFromOkay m?=${mFrom.mondexPretty()}")
                val (choiceFrom, resultFrom) = w.functions.startFrom(pd.from, mFrom)
                val (dashFrom, msg) = resultFrom
                println("1.3: m!=${msg.mondexPretty()}")
                dashFrom
            }

            system2.functions.update { w ->
                println("2: Create transfer to=${pd.from}, pd=${pd.mondexPretty()}")
                println("2.1: w=${w.mondexPretty()}")
                println("2.2: StartToOkay m?=${mTo.mondexPretty()}")
                val (choiceTo, resultTo) = w.functions.startTo(pd.to, mTo)
                val (dashTo, msg) = resultTo
                println("2.3: m!=${msg.mondexPretty()}")
                dashTo
            }
        },
        pre = { pd ->
            // Expect that neither startFrom/To have been setup yet
            forall(mk_Set(pd.from, pd.to)) { n ->
                n in system.world.conAuthPurse.dom &&
                system.world.conAuthPurse[n].pdAuth == null
            }
        },
        post = { pd, result ->
            val msgs = mk_Set(getMessage<StartFrom>(pd), getMessage<StartTo>(pd))
            msgs subset result.world.ether &&
            // Expect that startFrom/To have been setup
            forall(mk_Set(pd.from, pd.to)) { n ->
                n in result.world.conAuthPurse.dom &&
                    result.world.conAuthPurse[n].pdAuth != null
            }
            forall(msgs) { m ->
                as_CPDUnprotectedMessage(m).cpd.let { cpd ->
                    // StartTo target is set but not ready (waiting a StartTo call)
                    cpd.name in mk_Set(pd.from, pd.to) &&
                        cpd.name in result.world.conAuthPurse.dom &&
                        result.world.conAuthPurse[cpd.name].pdAuth != null
                }
            }
        },
    )

    val requestTransfer = function(
        command = { pd: PayDetails ->
            val mReq = getMessage<Req>(pd)
            update { w ->
                println("3: Request transfer pd=${pd.mondexPretty()}")
                println("3.1: w=${w.mondexPretty()}")
                println("3.2: ReqOkay m?=${mReq.mondexPretty()}")
                val (choiceTo, resultTo) = w.functions.reqOp(pd.from,mReq)
                val (dashTo, msg) = resultTo
                println("2.3: m!=${msg.mondexPretty()}")
                dashTo
            }
        },
        //TODO LF - Somewhat repeated; need a cleaner resulting interface for the post?
        pre = { pd -> system.world.functions.reqOp.pre(pd.from, getMessage<Req>(pd)) },
        //post = { pd, result -> system.world.functions.reqOp.post(pd.from, getMessage<Req>(pd), result.world) }
    )

    val sendTransfer = function(
        command = { pd: PayDetails ->
            val mVal = getMessage<Val>(pd)
            update { w ->
                println("4: Send transfer pd=${pd.mondexPretty()}")
                println("4.1: w=${w.mondexPretty()}")
                println("4.2: ReqOkay m?=${mVal.mondexPretty()}")
//                val (choiceTo, resultTo) = w.functions.valOp(pd.from,mVal)
//                val (dashTo, msg) = resultTo
//                println("4.3: m!=${msg.mondexPretty()}")
//                dashTo
                w
            }
        },
        pre = { pd -> system.world.functions.valOp.pre(pd.from, getMessage<Val>(pd)) },
    )

    val ackTransfer = function(
        command = { pd: PayDetails ->
            val mAck = getMessage<Ack>(pd)
            update { w ->
                println("5: Send transfer pd=${pd.mondexPretty()}")
                println("5.1: w=${w.mondexPretty()}")
                println("5.2: ReqOkay m?=${mAck.mondexPretty()}")
//                val (choiceTo, resultTo) = w.functions.ackOp(pd.from,mAck)
//                val (dashTo, msg) = resultTo
//                println("5.3: m!=${msg.mondexPretty()}")
//                dashTo
                w
            }
        },
        pre = { pd -> system.world.functions.ackOp.pre(pd.from, getMessage<Ack>(pd)) },
    )
}
