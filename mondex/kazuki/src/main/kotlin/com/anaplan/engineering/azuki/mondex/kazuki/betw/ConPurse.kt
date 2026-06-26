package com.anaplan.engineering.azuki.mondex.kazuki.betw

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.Purse
import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.betw.ConPurse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.betw.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.mondex.kazuki.betw.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.mondex.kazuki.betw.PayDetails_Module.transform
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
import org.w3c.dom.css.Counter

enum class Status { eaFrom, eaTo, epr, epv, epa }

const val MAX_NAT: nat = 10000UL

@Module
interface ConPurse : Purse {
    val exLog: Set<PayDetails>
    val name: Name
    val nextSeqNo: nat

    //LF @QST ConPurse invariant says the name must be in from or to. This represents the "last" payment done by this purse
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

    // Xi schemas with hidding need to be implemented either like an extra post check
    // or with some more sophisticated notion of alphabet via reflection to know what's hideen / can change.
    // Keeping it simple
    //
    // Xi ConPurse \ (nextSeqNo): everywhere equal but nextSeqNo
    private val xiConPurseIncrease = function(
        command = { before: ConPurse, after: ConPurse ->
            //TODO go with reflection over all declared fields of ConPurse but nextSeqNo and state equality; doing manually
            before.balance == after.balance &&
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
            before.balance == after.balance &&
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
                // The Z allows for no update at all as well; choosing to update
                    nextSeqNo = old.nextSeqNo + 1U,
                    status = Status.eaFrom,
                // Notice the Z doesn't say anything about what the result m! should be! Simply choosing one
                ), Message.Bottom)
        },
        // Z implicit pre! See ZEVES-PRG126 Table 8.2 p.86
        // Technically speaking for this operation in isolation, this is not needed
        // But for how it is used, in sequential composition with others, then it is!
        pre = { _ ->
            logIfNecessary.pre() &&
            old.name in setOf(old.pdAuth!!.from, old.pdAuth!!.to)
        },
        post = { mquery: Message, result ->
            val (dash, mr) = result
            xiConPurseAbort(old, dash) &&
                dash.nextSeqNo >= old.nextSeqNo &&
                mr == Message.Bottom &&
                logIfNecessary.post(dash)
        }
    )

    val validStartFrom = function(
        command = { m: Message, cpd: CounterPartyDetails ->
            old
        },
        pre = { m, cpd ->
            m == Message.StartFrom(cpd) &&
                cpd.name != old.name &&
                cpd.value <= old.balance
        },
    )

    // Xi ConPurse \ (nextSeqNo, pdAuth, status): everywhere equal but listed items
    private val xiConPurseStart = function(
        command = { before: ConPurse, after: ConPurse ->
            before.balance == after.balance &&
                before.exLog == after.exLog &&
                before.name == after.name //&&
                //before.nextSeqNo == after.nextSeqNo &&
                //before.pdAuth == after.pdAuth &&
                //before.status == after.status
        }
    )

    //LF @QST should this be a @Module (given the cpd), or just have it with input?
    val startFromPurseEaFromOkay = function(
        command = { m: Message, cpd: CounterPartyDetails ->
            val dash = validStartFrom(m, cpd)
            mk_(old.transform(
                        nextSeqNo = old.nextSeqNo + 1UL,
                        status = Status.epr,
                        pdAuth = mk_PayDetails(
                            from = old.name,
                            to = cpd.name,
                            value = cpd.value,
                            fromSeqNo = old.nextSeqNo,
                            toSeqNo = cpd.nextSeqNo,
                        )
                ),
                Message.Bottom
            )
        },
        // Z ConPurse is finite. CounterPartyDetails (and PayDetails) have `value: nat` that creates infinitely
        // many possible payments, which would not satisfy the finiteness of ConPurse Mapping type in Z (\ffun),
        //
        // In the Z/Eves proof, this required a underdefined upper bound, namely some `MAX\_NAT: nat`, which is
        // left unspecified, but exists, and sequence numbers must not go beyond it. This appears in the pre below.
        // This is one of the errors in the mondex proof discovered during the Z/Eves proof. We need a similar concept
        pre = { m, cpd ->
            old.pdAuth != null &&
                validStartFrom.pre(m, cpd) &&
                old.status == Status.eaFrom &&
                old.nextSeqNo < MAX_NAT
        },
        post = { m, cpd, result ->
            val (dash, mr) = result
            xiConPurseStart(old, dash) &&
            dash.nextSeqNo > old.nextSeqNo &&
            dash.status == Status.epr &&
            dash.pdAuth!!.from == old.name &&
            dash.pdAuth!!.to == cpd.name &&
            dash.pdAuth!!.value == cpd.value &&
            dash.pdAuth!!.fromSeqNo == old.nextSeqNo &&
            dash.pdAuth!!.toSeqNo == cpd.nextSeqNo &&
            mr == Message.Bottom
        }
    )

    // In Z, startFromPurseOkay \defs abortPurseOkay \semi (startFromPurseEaFromOkay \hide (cpd))
    //    * Hiding has higher precedence than composition, so the precondition here establishes the existence of a cpd
    //    * The way "aborting" works is that aborts run, but if nothing is to be logged, then aborting failed (i.e. no abort)
    //    * abort them must establish the pre of startFromPurseEaFromOkay, which can then execute; further pres are needed
    //      for hiding of cpd
    val startFromPurseOkay = function(
        command = { m: Message ->
            // Abort's message result is ignored
            val (dash, _) = abortPurseOkay(m)
            val cpd = mk_CounterPartyDetails(
                name = TODO(),
                value = TODO(),
                nextSeqNo = TODO())
            // startFromPurseEaFromOkay works on the resulting state of abort with cpd hidden
            mk_(dash.functions.startFromPurseEaFromOkay(m, cpd), Message.Bottom)
        },
        pre = { m ->
            abortPurseOkay.pre(m)
        },
        post = { m, result ->
            // abortPurseOkay \semi (startFromPurseEaFromOkay \hide (cpd))
            // = [Z]
            // exists ConPurse_0 & abortPurseOkay[ConPurse_0/ConPurse'] and
            //   (exists cpd: CounterPartyDetails & startFromPurseEaFromOkay[ConPurse_0/ConPurse])
            // = [in KSpec]
            // middle = old.abordPurseOkay(m) and (exists cpd & middle.startFromPurseEaFromOkay(m, cpd, dash)
            val (middle, mm) = abortPurseOkay(m)
            val (dash, mr) = result
            val cpd = mk_CounterPartyDetails(
                name = TODO(),
                value = TODO(),
                nextSeqNo = TODO())
            // check abort post from start to middle; check startFrom post from middle to dash
            abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.startFromPurseEaFromOkay.post(m, cpd, dash)
        }
    )
}
