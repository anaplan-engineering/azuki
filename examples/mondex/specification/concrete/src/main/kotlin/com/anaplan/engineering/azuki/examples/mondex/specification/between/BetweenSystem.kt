package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.WorldSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.WorldSystemFunctions
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.as_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.is_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.mk_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem_Module.mk_BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CPDUnprotectedMessage_Module.as_CPDUnprotectedMessage
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.mk_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.as_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.is_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.mk_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.as_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.is_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.mk_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.as_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.is_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.mk_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.as_Val
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.is_Val
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.mk_Val
import com.anaplan.engineering.azuki.examples.mondex.specification.mondexPretty
import com.anaplan.engineering.azuki.examples.mondex.specification.then
import com.anaplan.engineering.kazuki.core.*

@Module
interface BetweenSystem: WorldSystem {

    override val world: BetweenWorld
    //val last: Message
    val last: Message

    @FunctionProvider(BetweenSystemFunctions::class)
    override val functions: BetweenSystemFunctions
}

typealias BetweenWorldMonad = (Tuple2<BetweenWorld, Message>) -> Tuple2<BetweenWorld, Message>
// World forward composition: (f ; g)(W) = g(f(W)), shame `;` not possible
infix fun <W> ((W) -> W).then(next: (W) -> W): (W) -> W = { monad -> next(this(monad)) }

const val PRINT_BEFORE = false

class BetweenSystemFunctions(val system: BetweenSystem) : WorldSystemFunctions() {
    // BetweenWorld delegated receiver to perform world updates functionally in monadic style
    fun update(modify: BetweenWorldMonad): BetweenSystem {
        val (after, last) = modify(mk_(system.world, system.last))
        return updateSystem(after, last)
    }

    fun updateSteps(vararg steps: BetweenWorldMonad) =
        update(steps.reduce { acc, step -> acc then step })

    private val updateSystem = function(
        command = { after: BetweenWorld, last: Message -> mk_BetweenSystem(after, last) },
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
//    val startTransfer = function(
//        command = { pd: PayDetails ->
//
//            // We are trying to model here the BetweenInitState, which will include
//            // the initial ether with partially started messages for startFrom and startTo.
//            val mFrom = getMessage<StartFrom>(pd)
//            val mTo = getMessage<StartTo>(pd)
//
//            //@4paper - Explicitly changing the ether is dangerous, but the invariants of BetweenWorld will enforce correctness
//            //          We need to do that explicitly, given we don't have infinite paydetails for BetweenInitState
//            val system1 = update { w -> w.transform(ether = w.ether + mk_Set(mFrom, mTo)) }
//
//            //TODO LF SF - inner specification of these not being checked; Would be if we had a compose operator
//            val system2 = system1.functions.update { w ->
//                println("1: Create transfer from=${pd.from}, pd=${pd.mondexPretty()}")
//                println("1.1: w=${w.mondexPretty()}")
//                println("1.2: StartFromOkay m?=${mFrom.mondexPretty()}")
//                val (choiceFrom, resultFrom) = w.functions.startFrom(pd.from, mFrom)
//                val (dashFrom, msg) = resultFrom
//                println("1.3: m!=${msg.mondexPretty()}")
//                dashFrom
//            }
//
//            system2.functions.update { w ->
//                println("2: Create transfer to=${pd.from}, pd=${pd.mondexPretty()}")
//                println("2.1: w=${w.mondexPretty()}")
//                println("2.2: StartToOkay m?=${mTo.mondexPretty()}")
//                val (choiceTo, resultTo) = w.functions.startTo(pd.to, mTo)
//                val (dashTo, msg) = resultTo
//                println("2.3: m!=${msg.mondexPretty()}")
//                dashTo
//            }
//        },
//        pre = { pd ->
//            // Expect that neither startFrom/To have been setup yet
//            forall(mk_Set(pd.from, pd.to)) { n ->
//                n in system.world.conAuthPurse.dom &&
//                system.world.conAuthPurse[n].pdAuth == null
//            }
//        },
//        post = { pd, result ->
//            val msgs = mk_Set(getMessage<StartFrom>(pd), getMessage<StartTo>(pd))
//            msgs subset result.world.ether &&
//            // Expect that startFrom/To have been setup
//            forall(mk_Set(pd.from, pd.to)) { n ->
//                n in result.world.conAuthPurse.dom &&
//                    result.world.conAuthPurse[n].pdAuth != null
//            }
//            forall(msgs) { m ->
//                as_CPDUnprotectedMessage(m).cpd.let { cpd ->
//                    // StartTo target is set but not ready (waiting a StartTo call)
//                    cpd.name in mk_Set(pd.from, pd.to) &&
//                        cpd.name in result.world.conAuthPurse.dom &&
//                        result.world.conAuthPurse[cpd.name].pdAuth != null
//                }
//            }
//        },
//    )

    val startTransferFrom = function(
        command = { name: Name, m: Message ->

//            // We are trying to model here the BetweenInitState, which will include
//            // the initial ether with partially started messages for startFrom and startTo.
//            val mFrom = getMessage<StartFrom>(pd)

            //@4paper - Explicitly changing the ether is dangerous, but the invariants of BetweenWorld will enforce correctness
            //          We need to do that explicitly, given we don't have infinite paydetails for BetweenInitState
            updateSteps(
                { monad -> val (w, _) = monad; mk_(w.transform(ether = w.ether + mk_Set(m)), m) },
                { monad -> val (w, _) = monad
                    println("1: StartFrom name?=${name}, m?=${m.mondexPretty()}")
                    if (PRINT_BEFORE) println ("1.1: w=${w.mondexPretty()}")
                    val (choice, result) = w.functions.startFrom(name, m)
                    val (dash, msg) = result
                    println("1.2: m!=${msg.mondexPretty()}, choice=$choice")
                    println("1.3: w'=${dash.mondexPretty()}")
                    result
                }
            )
        },
        pre = { name, _ ->
            // Expect that neither startFrom/To have been setup yet
            name in system.world.conAuthPurse.dom && system.world.conAuthPurse[name].pdAuth == null
        },
        post = { name, m, result ->
            m in result.world.ether &&
                // Expect that startFrom/To have been setup
                name in result.world.conAuthPurse.dom &&
                    result.world.conAuthPurse[name].pdAuth != null &&
                is_StartFrom(m) && as_StartFrom(m).cpd.let { cpd ->
                    // StartTo target is set but not ready (waiting a StartTo call)
                    cpd.name in result.world.conAuthPurse.dom &&
                        result.world.conAuthPurse[cpd.name].pdAuth == null
                }
        },
    )

    val startTransferTo = function(
        command = { name: Name, m: Message ->
            updateSteps(
                { monad -> val (w, _) = monad; mk_(w.transform(ether = w.ether + mk_Set(m)), m) },
                { monad -> val (w, _) = monad
                    println("2: StartTo name?=${name}, m?=${m.mondexPretty()}")
                    if (PRINT_BEFORE) println("2.1: w=${w.mondexPretty()}")
                    val (choice, result) = w.functions.startTo(name, m)
                    val (dash, msg) = result
                    println("2.2: m!=${msg.mondexPretty()}, choice=$choice")
                    println("2.3: w'=${dash.mondexPretty()}")
                    result
                }
            )
        },
        pre = { name, m ->
            // Expect that neither startFrom/To have been setup yet
            name in system.world.conAuthPurse.dom && system.world.conAuthPurse[name].pdAuth == null
            is_StartTo(m) && as_StartTo(m).cpd.let { cpd ->
                cpd.name in system.world.conAuthPurse.dom &&
                    system.world.conAuthPurse[cpd.name].pdAuth != null
            }
        },
        post = { name, m, result ->
            name in result.world.conAuthPurse.dom &&
            result.world.conAuthPurse[name].pdAuth != null &&
            // Ether has been updated with an appropriate `req pd` message from startTo
            result.last in result.world.ether &&
                is_Req(result.last) &&
                as_Req(result.last).pd.to == name
        }
    )

    val requestTransfer = function(
        //TODO LF should name = as_Req(m).pd.from/to?
        command = { name: Name, m: Message ->
            update { monad -> val (w, _) = monad;
                println("3: Request transfer name?=${name.mondexPretty()}, m?=${m.mondexPretty()}")
                if (PRINT_BEFORE) println("3.1: w=${w.mondexPretty()}")
                val (choice, result) = w.functions.reqOp(name,m)
                val (dash, msg) = result
                println("3.2: m!=${msg.mondexPretty()}, choice=$choice")
                println("3.3: w'=${dash.mondexPretty()}")
                result
            }
        },
        pre = { name, m -> system.world.functions.reqOp.pre(name, m) },
        post = { name, m, result ->
            result.last in result.world.ether &&
                is_Val(result.last) &&
                // ConPurse invariant on epr
                as_Val(result.last).pd.from == name
        }
    )

    val sendTransfer = function(
        //TODO LF should name = as_Req(m).pd.from/to?
        command = { name: Name, m: Message ->
            update { monad -> val (w, _) = monad
                println("4: Send transfer name?=${name.mondexPretty()}, m?=${m.mondexPretty()}")
                if (PRINT_BEFORE) println("4.1: w=${w.mondexPretty()}")
                val (choice, result) = w.functions.valOp(name,m)
                val (dash, msg) = result
                println("4.2: m!=${msg.mondexPretty()}, choice=$choice")
                println("4.3: w'=${w.mondexPretty()}")
                result
            }
        },
        pre = { name, m -> system.world.functions.valOp.pre(name, m) },
        post = { name, m, result ->
            result.last in result.world.ether && is_Ack(result.last)
        }

    )

    val ackTransfer = function(
        command = { name: Name, m: Message ->
            update { monad -> val (w, _) = monad
                println("5: Ack transfer name?=${name.mondexPretty()}, m?=${m.mondexPretty()}")
                if (PRINT_BEFORE) println("5.1: w=${w.mondexPretty()}")
                val (choice, result) = w.functions.ackOp(name,m)
                val (dash, msg) = result
                println("5.2: m!=${msg.mondexPretty()}, choice=$choice")
                println("5.3: w'=${dash.mondexPretty()}")
                result
            }
        },
        pre = { name, m -> system.world.functions.ackOp.pre(name, m) },
        post = { name, m, result ->
            result.last in result.world.ether && result.last is Bottom
        }
    )

    val abortTransfer = function(
        command = { name: Name, m: Message ->
            update { monad -> val (w, _) = monad
                println("6: Abort transfer name?=${name.mondexPretty()}, m?=${m.mondexPretty()}")
                if (PRINT_BEFORE) println("6.1: w=${w.mondexPretty()}")
                val (choice, result) = w.functions.abort(name,m)
                val (dash, msg) = result
                println("6.2: m!=${msg.mondexPretty()}, choice=$choice")
                println("6.3: w'=${dash.mondexPretty()}")
                result
            }
        },
        pre = { name, m -> system.world.functions.abort.pre(name, m) },
        post = { name, m, result -> result.last in result.world.ether },
    )
}
