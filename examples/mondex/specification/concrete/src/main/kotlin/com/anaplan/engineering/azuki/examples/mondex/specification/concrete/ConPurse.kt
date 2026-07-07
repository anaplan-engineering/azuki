package com.anaplan.engineering.azuki.examples.mondex.specification.concrete

import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Name
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.image
import com.anaplan.engineering.kazuki.core.*
import kotlin.collections.contains
import kotlin.collections.isNotEmpty
import kotlin.collections.map

//LF Perhaps reuse from adapter-api?
enum class Status { eaFrom, eaTo, epr, epv, epa }

const val MAX_NAT: nat = 10000UL

@Module
interface ConPurse {
    val balance: nat
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
class ConPurseFunctions(private val old: ConPurse) {

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
        command = { _: Message ->
            mk_(old.transform(nextSeqNo = old.nextSeqNo + 1U), Message.Bottom)
        },
        // Keep the explicit `true` to document none is needed, explicitly
        pre = { _ -> true },
        post = { _: Message, result ->
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
        command = { _: Message ->
            val dash = logIfNecessary()
            mk_(dash.transform(
                // The Z allows for no update at all as well; choosing to update
                    nextSeqNo = old.nextSeqNo + 1U,
                    status = Status.eaFrom,
                // Notice the Z doesn't say anything about what the result m! should be! Simply choosing one
                ), Message.Bottom)
        },
        // Z implicit pre! See ZEVES-PRG126 Table 8.2 p.86
        // * ZEVES-PRG126 Theorem 8.6: issue on at least two names being needed (AbortPurseOkay is not total as PRG126 claims)!
        // * ConPurse.nameLogged() invariant, which is the justification for this precondition we discovered
        // * Because of the underdefinedness of pdAuth (PRG126 Sect. 4.8.2), its validity (ConPurse Invariants) is the responsibility of caller
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
            m is Message.StartFrom &&
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

    //LF @QST need a mechanism to creating a different name and smaller or equal balance; can the `private val old` work?
    //TODO this needs to "repeat" in certain places, and be new in others (cached)
    internal fun arbitraryCPD() =
        mk_CounterPartyDetails(
            name = old.name + "cpd",
            value = old.balance - 1U,
            nextSeqNo = old.nextSeqNo,
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
            // startFromPurseEaFromOkay works on the resulting state of abort with cpd hidden
            dash.functions.startFromPurseEaFromOkay(m, arbitraryCPD())
        },
        pre = { m ->
            abortPurseOkay.pre(m) &&
                startFromPurseEaFromOkay.pre(m, arbitraryCPD())
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
            // check abort post from start to middle; check startFrom post from middle to dash
            abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.startFromPurseEaFromOkay.post(m, arbitraryCPD(), result)
        }
    )

    val validStartTo = function(
        command = { m: Message, cpd: CounterPartyDetails ->
            old
        },
        pre = { m, cpd ->
            m == Message.StartTo(cpd)
                && cpd.name != old.name
        },
    )

    val startToPurseEaFromOkay = function(
        command = { m: Message, cpd: CounterPartyDetails ->
            val dash = validStartTo(m, cpd)
            val newPdAuth = mk_PayDetails(
                to = old.name,
                from = cpd.name,
                value = cpd.value,
                toSeqNo = old.nextSeqNo,
                fromSeqNo = cpd.nextSeqNo,
            )
            mk_(old.transform(
                nextSeqNo = old.nextSeqNo + 1UL,
                status = Status.epv,
                pdAuth = newPdAuth
            ),
                Message.Req(newPdAuth))
        },
        pre = { m, cpd ->
            old.pdAuth != null &&
                validStartTo.pre(m, cpd) &&
                old.status == Status.eaFrom &&
                old.nextSeqNo < MAX_NAT &&
                cpd.nextSeqNo < old.nextSeqNo
        },
        post = { m, cpd, result ->
            val (dash, mr) = result
            xiConPurseStart(old, dash) &&
                dash.nextSeqNo > old.nextSeqNo &&
                dash.pdAuth!!.to == old.name &&
                dash.pdAuth!!.from == cpd.name &&
                dash.pdAuth!!.value == cpd.value &&
                dash.pdAuth!!.toSeqNo == old.nextSeqNo &&
                dash.pdAuth!!.fromSeqNo == cpd.nextSeqNo &&
                dash.status == Status.epv &&
                mr == Message.Req(dash.pdAuth!!)
        }
    )

    val startToPurseOkay = function(
        command = { m: Message ->
            val (dash, _) = abortPurseOkay(m)
            dash.functions.startToPurseEaFromOkay(m, arbitraryCPD())
        },
        pre = { m ->
            abortPurseOkay.pre(m) &&
                startToPurseEaFromOkay.pre(m, arbitraryCPD())
        },
        post = { m, result ->
            val (middle, mm) = abortPurseOkay(m)
            val (dash, mr) = result
            abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.startToPurseEaFromOkay.post(m, arbitraryCPD(), result)

        }
    )

    val authenticReqMessage = function(
        command = { m: Message ->
            old
        },
        pre = { m ->
            m == Message.Req(old.pdAuth!!)
        },
    )

    private val xiConPurseReq = function(
        command = { before: ConPurse, after: ConPurse ->
            //before.balance == after.balance &&
                before.exLog == after.exLog &&
                before.name == after.name &&
                before.nextSeqNo == after.nextSeqNo &&
                before.pdAuth == after.pdAuth //&&
                //before.status == after.status
        }
    )

    val reqPurseOkay = function(
        command = { m: Message ->
            val dash = authenticReqMessage(m)
            mk_(old.transform(
                balance = old.balance - old.pdAuth!!.value,
                status = Status.epa
            ),
                Message.Val(old.pdAuth!!)
            )
        },
        pre = { m ->
            old.pdAuth != null &&
                old.status == Status.epr &&
                m == Message.Req(old.pdAuth!!)
        },
        post = { m, result ->
            val (dash, mr) = result
            xiConPurseReq(old, dash) &&
                dash.balance == old.balance - old.pdAuth!!.value &&
                dash.status == Status.epa &&
                mr == Message.Val(old.pdAuth!!)
        }
    )

    val authenticValMessage = function(
        command = { m: Message ->
            old
        },
        pre = { m ->
            m == Message.Val(old.pdAuth!!)
        }
    )

    private val xiConPurseVal = function(
        command = { before: ConPurse, after: ConPurse ->
            //before.balance == after.balance &&
                before.exLog == after.exLog &&
                before.name == after.name &&
                before.nextSeqNo == after.nextSeqNo &&
                before.pdAuth == after.pdAuth //&&
                //before.status == after.status
        }
    )

    val valPurseOkay = function(
        command = { m: Message ->
            val dash = authenticValMessage(m)
            mk_(old.transform(
                balance = old.balance + old.pdAuth!!.value,
                status = Status.eaTo
            ), Message.Ack(old.pdAuth!!))
        },
        pre = { m ->
            old.pdAuth != null &&
                old.status == Status.epv &&
                m == Message.Val(old.pdAuth!!)
        },
        post = { m, result ->
            val (dash, mr) = result
            xiConPurseVal(old, dash) &&
                dash.balance == old.balance + old.pdAuth!!.value &&
                dash.status == Status.eaTo &&
                mr == Message.Ack(old.pdAuth!!)
        }
    )

    val authenticAckMessage = function(
        command = { m: Message ->
            old
        },
        pre = { m ->
            m == Message.Ack(old.pdAuth!!)
        }
    )

    private val xiConPurseAck = function(
        command = { before: ConPurse, after: ConPurse ->
            before.balance == after.balance &&
            before.exLog == after.exLog &&
                before.name == after.name &&
                before.nextSeqNo == after.nextSeqNo //&&
                //before.pdAuth == after.pdAuth &&
            //before.status == after.status
        }
    )

    val ackPurseOkay = function(
        command = { m: Message ->
            val dash = authenticAckMessage(m)
            mk_(old.transform(
                status = Status.eaFrom
            ), Message.Bottom)
        },
        pre = { m ->
            old.pdAuth != null &&
                old.status == Status.epa &&
                m == Message.Ack(old.pdAuth!!)
        },
        post = { m, result ->
            val (dash, mr) = result
            xiConPurseAck(old, dash) &&
                dash.status == Status.eaFrom &&
                mr == Message.Bottom
        }
    )

    val readExceptionLogPurseEaFromOkay = function(
        command = { m: Message ->
            val mr = if (old.exLog.isEmpty()) Message.Bottom
                else Message.ExceptionLogResult(old.name, old.exLog.arbitrary())
            mk_(old, mr)
        },
        pre = { m ->
            old.status == Status.eaFrom &&
                m == Message.ReadExceptionLog
        },
        post = { m, result ->
            val (dash, mr) = result
            dash == old && (
                mr == Message.Bottom || mr in dash.exLog.map { ld -> Message.ExceptionLogResult(dash.name, ld) })
        }
    )

    val readExceptionLogPurseOkay = function(
        command = { m: Message ->
            val (dash, _) = abortPurseOkay(m)
            dash.functions.readExceptionLogPurseEaFromOkay(m)
        },
        pre = { m ->
            abortPurseOkay.pre(m) &&
                readExceptionLogPurseEaFromOkay.pre(m)
        },
        post = { m, result ->
            val (middle, mm) = abortPurseOkay(m)
            val (dash, mr) = result
            abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.readExceptionLogPurseEaFromOkay.post(m, result)
        }
    )

    private val xiConPurseClear = function(
        command = { before: ConPurse, after: ConPurse ->
            before.balance == after.balance &&
                //before.exLog == after.exLog &&
                before.name == after.name &&
                before.nextSeqNo == after.nextSeqNo &&
                before.pdAuth == after.pdAuth &&
                before.status == after.status
        }
    )

    val clearExceptionLogPurseEaFromOkay = function(
        command = { m: Message ->
            mk_(old.transform(
                exLog = emptySet<PayDetails>()
            ), Message.Bottom
            )
        },
        pre = { m ->
            old.status == Status.eaFrom &&
                old.exLog.isNotEmpty() &&
                m == Message.ExceptionLogClear(old.name,
                image(
                    as_Set1(old.exLog)))
        },
        post = { m, result ->
            val (dash, mr) = result
            xiConPurseClear(old, dash) &&
                dash.exLog.isEmpty() &&
                mr == Message.Bottom
        }
    )

    val clearExceptionLogPurseOkay = function(
        command = { m: Message ->
            val (dash, _) = abortPurseOkay(m)
            dash.functions.clearExceptionLogPurseEaFromOkay(m)
        },
        pre = { m ->
            abortPurseOkay.pre(m) &&
                old.exLog.isNotEmpty() &&
                m == Message.ExceptionLogClear(old.name,
                image(
                    as_Set1(old.exLog).union(
                        if (old.status in mk_Set(Status.epv, Status.epa)) mk_Set(old.pdAuth) else emptySet()
                    )))
        },
        post = { m, result ->
            val (middle, mm) = abortPurseOkay(m)
            val (dash, mr) = result
            abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.clearExceptionLogPurseEaFromOkay.post(m, result)
        }
    )
}
