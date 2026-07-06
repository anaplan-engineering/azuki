package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.mk_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem_Module.mk_BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.mk_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.as_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.is_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.mk_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.as_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.is_StartTo
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
    //
    // TODO we are trying to model here the BetweenInitState, which will include the initial ether with partially started messages for startFrom and startTo!
    val startTransfer = function(
        command = { pd: PayDetails ->

            val mFrom = getMessage<StartFrom>(pd)
            val mTo = getMessage<StartTo>(pd)

            //TODO Explicitly changing the ether like this should not be allowed!
            val system1 = update { w -> w.transform(ether = w.ether + mk_Set(mFrom, mTo)) }

            //TODO LF - inner specification of these not being checked !!! This is not quite right
            val system2 = system1.functions.update { w ->
                println("1: Create transfer from=${pd.from}, pd=${pd.mondexPretty()}")
                println("1.1: w1=${w.mondexPretty()}")
                println("1.2: StartFromOkay m?=${mFrom.mondexPretty()}")
                val (choiceFrom, resultFrom) = w.functions.startFrom(pd.from, mFrom)
                val (dashFrom, msg) = resultFrom
                println("1.3: m!=${msg.mondexPretty()}")
                dashFrom
            }

            val system3 = system2.functions.update { w ->
                println("2: Create transfer to=${pd.from}, pd=${pd.mondexPretty()}")
                println("2.1: w2=${w.mondexPretty()}")
                println("2.2: StartToOkay m?=${mTo.mondexPretty()}")
                val (choiceTo, resultTo) = w.functions.startTo(pd.to, mTo)
                val (dashTo, msg) = resultTo
                println("2.3: m!=${msg.mondexPretty()}")
                dashTo
            }
            system3
        },
    )

    val requestTransfer = function(
        command = { pd: PayDetails ->
            val mReq = getMessage<Req>(pd)
            update { w ->
                println("3: Request transfer pd=${pd.mondexPretty()}")
                println("3.1: w1=${w.mondexPretty()}")
                println("3.2: ReqOkay m?=${mReq.mondexPretty()}")
                val (choiceTo, resultTo) = w.functions.reqOp(pd.from,mReq)
                val (dashTo, msg) = resultTo
                println("2.3: m!=${msg.mondexPretty()}")
                dashTo
            }
        }
    )

    // We are trying to model here the BetweenInitState, which will include
    // the initial ether with partially started messages for startFrom and startTo.
    //
    // Found having both startFrom/To together got confusing, given the way they set each other
    private val establishValidPayDetailsFrom = function(
        command = { pd: PayDetails ->
            val fromPurse = system.world.conAuthPurse[pd.from]
            val toPurse = system.world.conAuthPurse[pd.to]
            // StartFrom message gets the toPurse's name and seq no;
            val mFrom = mk_StartFrom(
                mk_CounterPartyDetails(
                    toPurse.name,
                    pd.value,
                    toPurse.nextSeqNo,
                )
            )
            // Given how we (did not have as in PRG) BetweenInitState, we must update the ether
            mk_(system.world.transform(ether = system.world.ether + mk_Set(mFrom)), mFrom)
        },
        pre = { pd ->
            // Expect that neither startFrom/To have been setup yet
            forall(mk_Set(pd.from, pd.to)) { n ->
                n in system.world.conAuthPurse.dom //&&
                    //system.world.conAuthPurse[n].pdAuth == null
            }
        },
        post = { pd, result ->
            val (dash, m) = result
            m in dash.ether &&
                dash.conAuthPurse[pd.from].pdAuth == null &&
                is_StartFrom(m) &&
                as_StartFrom(m).cpd.let { cpd ->
                    // StartTo target is set but not ready (waiting a StartTo call)
                    cpd.name == pd.to &&
                        cpd.name in dash.conAuthPurse.dom &&
                        dash.conAuthPurse[cpd.name].pdAuth == null
                }
        },
    )

    private val establishValidPayDetailsTo = function(
        command = { pd: PayDetails ->
            val fromPurse = system.world.conAuthPurse[pd.from]
            val toPurse = system.world.conAuthPurse[pd.to]
            // StartTo message gets the fromPurse's name and seq no;
            val mTo = mk_StartTo(
            mk_CounterPartyDetails(
                fromPurse.name,
                pd.value,
                fromPurse.nextSeqNo,
                )
            )
            mk_(system.world.transform(ether = system.world.ether + mk_Set(mTo)), mTo)
        },
        pre = { pd ->
            // Expect that startFrom have been setup; startTo not yet
            mk_Set(pd.from, pd.to) subset system.world.conAuthPurse.dom &&
                system.world.conAuthPurse[pd.from].pdAuth != null &&
                system.world.conAuthPurse[pd.to].pdAuth == null
        },
        post = { pd, result ->
            val (dash, m) = result
            m in dash.ether &&
                dash.conAuthPurse[pd.to].pdAuth == null &&
                is_StartTo(m) &&
                //TODO remove? Somewhat redundant
                as_StartTo(m).cpd.let { cpd ->
                    cpd.name == pd.from &&
                        cpd.name in dash.conAuthPurse.dom &&
                        dash.conAuthPurse[cpd.name].pdAuth != null
                }
        },
    )
}
