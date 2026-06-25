package com.anaplan.engineering.azuki.mondex.kazuki.betw

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.Purse
import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.betw.ConPurse_Module.transform
import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Tuple2
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.forall
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.nat

enum class Status { eaFrom, eaTo, epr, epv, epa }

@Module
interface ConPurse : Purse {
    val exLog: Set<PayDetails>
    val name: Name
    val nextSeqNo: nat

    //LF: ConPurse invariant says the name must be in from or to. This represents the "last" payment done by this purse
    //    To bootstrap (first purse), you might need to have here something that might be null, given you can't have a
    //    payment to yourself. Or allow only when status = eaFrom?
    val pdAuth: PayDetails?
    val status: Status

    @Invariant
    fun nameLogged() = forall(exLog) { pd -> name in as_Set(setOf(pd.from, pd.to)) }

    @Invariant
    fun eprStatusP1() =
        (status == Status.epr) implies {
            pdAuth != null &&
                name == pdAuth!!.from &&
                pdAuth!!.value <= balance &&
                pdAuth!!.fromSeqNo < nextSeqNo
        }

    @Invariant
    fun epvStatusP2() =
        (status == Status.epv) implies {
            pdAuth != null &&
            pdAuth!!.toSeqNo < nextSeqNo
        }

    @Invariant
    fun epaStatusP3() =
        (status == Status.epa) implies {
            pdAuth != null &&
                pdAuth!!.fromSeqNo < nextSeqNo
        }

    @FunctionProvider(ConPurseFunctions::class)
    val functions: ConPurseFunctions

}

// * Z pres are implicit. Get them from ZEVES-PRG126 Table 8.2 p.86
// * This provider contains the before state, and would expect the
class ConPurseFunctions(old: ConPurse) {

//    val increasePurseOkayAsZ = function(
//        command = { dash: ConPurse, _: Message ->
//            //dash.nextSeqNo = old.nextSeqNo + 1U
//            Message.Bottom
//        },
//        // Keep the explicit `true` to document none is needed, explicitly
//        pre = { _, _ -> true },
//        // This would entail providing an after state for checking
//        post = { dash, _, mr: Message ->
//            dash.nextSeqNo >= old.nextSeqNo &&
//            mr == Message.Bottom }
//    )

    // Xi schemas with hidding need to be implemented either like an extra post check
    // or with some more sophisticated notion of alphabet via reflection to know what's hideen / can change.
    // Keeping it simple
    //
    // Xi ConPurse \ (nextSeqNo): everywhere equal but nextSeqNo
    private val xiConPurseIncrease = function(
        command = { before: ConPurse, after: ConPurse ->
            //TODO go with reflection over all declared fields of ConPurse but nextSeqNo and state equality; doing manually
            before.exLog == after.exLog &&
                before.name == after.name &&
                //before.nextSeqNo == after.nextSeqNo &&
                before.pdAuth == after.pdAuth &&
                before.status == after.status
        }
    )

    val increasePurseOkay = function(
        command = { mquery: Message ->
            mk_(old.transform(nextSeqNo = old.nextSeqNo + 1U), Message.Bottom)
        },
        // Keep the explicit `true` to document none is needed, explicitly
        pre = { _ -> true },
        post = { mquery: Message, result ->
            val (dash, mr) = result
            xiConPurseIncrease(old, dash) &&
            dash.nextSeqNo >= old.nextSeqNo &&
                mr == Message.Bottom
        }
    )

    // Xi ConPurse \ (nextSeqNo, exLog, pdAuth, status): everywhere equal but listed items; AKA only name equal
    private val xiConPurseAbort = function(
        command = { before: ConPurse, after: ConPurse ->
            //before.exLog == after.exLog &&
                before.name == after.name //&&
                //before.nextSeqNo == after.nextSeqNo &&
                //before.pdAuth == after.pdAuth &&
                //before.status == after.status
        }
    )

    val logIfNecessary = function(
        command = { ->
            val log = (if (old.status in setOf(Status.epv, Status.epa)) mk_Set(old.pdAuth!!) else mk_Set())
            old.transform(exLog = old.exLog + log)
        },
        pre = { -> true },
        // The Z is always the post; often commands will be close to post
        post = { dash ->
            dash.exLog == old.exLog + (if (old.status in setOf(Status.epv, Status.epa)) mk_Set(old.pdAuth!!) else mk_Set())
        }
    )

    val abortPurseOkay = function(
        command = { mquery: Message ->
            val dash = logIfNecessary()
            mk_(dash.transform(
                    nextSeqNo = old.nextSeqNo + 1U,
                    status = Status.eaFrom,
                // Notice the Z doesn't say anything about what the result m! should be! Simply choosing one
                ), Message.Bottom)
        },
        // Z implicit pre! See ZEVES-PRG126 Table 8.2 p.86
        // Technically speaking for this operation in isolation, this is not needed
        // But for how it is used, in sequential composition with others, then it is!
        pre = { _ -> old.name in setOf(old.pdAuth!!.from, old.pdAuth!!.to) },
        post = { mquery: Message, result ->
            val (dash, mr) = result
            xiConPurseAbort(old, dash) &&
                dash.nextSeqNo >= old.nextSeqNo &&
                mr == Message.Bottom
        }
    )

}

