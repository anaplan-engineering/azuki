package com.anaplan.engineering.azuki.examples.mondex.specification.concrete

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.mk_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Bottom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Clear_Module.mk_Clear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear_Module.mk_ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.mk_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Name
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ReadExceptionLog
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.mk_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.as_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.is_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.as_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.is_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.mk_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.mk_Val
import com.anaplan.engineering.kazuki.core.*
import kotlin.collections.contains
import kotlin.collections.isNotEmpty
import kotlin.collections.map

// Progress through a transaction as:
// * eaFrom: expecting any payer,
// * eaTo  : expecting any payee,
// * epr   : expecting payment request,
// * epv   : expecting payment value transfer,
// * epa   : expecting payment acknowledgement.
enum class Status { eaFrom, eaTo, epr, epv, epa }

// Z ConPurse is finite. CounterPartyDetails (and PayDetails) have `value: nat` that creates infinitely
// many possible payments, which would not satisfy the finiteness of ConPurse Mapping type in Z (\ffun),
//
// Any nat would do; ZEVES-PRG126 also imposes that NAT = 0..MAX_NAT, where MAX_NAT is underspecified.
// It uses NAT instead of \nat as well. In Kazuki, because we are already finite anyhow, we can stick
// to nat and the MAX_NAT explicit bound.
const val MAX_NAT: nat = 10000UL

@Module
interface ConPurse {
    val balance: nat
    val exLog: Set<PayDetails>
    val name: Name
    val nextSeqNo: nat

    // This represents the "last" payment done by this purse.
    // To bootstrap (first purse), we need to have null pdAuth,
    // given you can't have a payment to yourself.
    //
    // This is allowed only for unprotected messages (StartFrom/To),
    // both of which has eaFrom status, so add the non-null invariant/property
    val pdAuth: PayDetails?
    val status: Status

    // pdAuth cannot be null outside of EaFrom
    @Invariant
    fun purseBootstrapping() = (status != Status.eaFrom) implies { pdAuth != null }

    @Invariant
    fun nameLogged() = forall(exLog) { pd -> name in as_Set(setOf(pd.from, pd.to)) }

    // Following PRG126 CoNPurse name convention for each invariant
    @Invariant
    fun p1_EprStatus() =
        (status == Status.epr) implies {
            pdAuth != null &&
                name == pdAuth!!.from &&
                pdAuth!!.value <= balance &&
                pdAuth!!.fromSeqNo < nextSeqNo
        }

    @Invariant
    fun p2_epvStatus() =
        (status == Status.epv) implies {
            pdAuth != null &&
            pdAuth!!.toSeqNo < nextSeqNo
        }

    @Invariant
    fun p3_epaStatus() =
        (status == Status.epa) implies {
            pdAuth != null &&
                pdAuth!!.fromSeqNo < nextSeqNo
        }

    @FunctionProvider(ConPurseProperties::class)
    val properties: ConPurseProperties

    @FunctionProvider(ConPurseFunctions::class)
    val functions: ConPurseFunctions
}

class ConPurseProperties(private val old: ConPurse) {

    //@4paper - surfaced mondex bootstrapping detail
    // After eaFrom, pdAuth cannot be null (i.e. only allowed for one of the two unprotected msgs)
    val pdAuth by property(
        pre = { old.pdAuth != null },
        post = { pdAuth -> pdAuth == old.pdAuth }
) { old.pdAuth!! }
}

// * Z pres are implicit. Get them from ZEVES-PRG126 Table 8.2 p.86
// * Actual details on schemas / theorems
// * This provider contains the before state, and would expect the
class ConPurseFunctions(private val old: ConPurse) {

    private val xiConPurse = function(
        command = { before: ConPurse, after: ConPurse -> before == after }
    )

    // Used for harmonizing PhiBOp post choices; needs type signature to avoid Bottom instead of Message
    internal val ignorePurse: VFunction1<Message, Tuple2<ConPurse, Message>> = function(
        command = { _: Message -> mk_(old, Bottom) },
        pre = { _ -> true },
        post = { _, result -> xiConPurse(old, result._1) }
    )

    //@4paper - inspired Kazuki extension
    // Xi schemas with hidding need to be implemented either like an extra post check
    // or with some more sophisticated notion of alphabet via reflection to know what's hideen / can change.
    // Keeping it simple
    //
    // Xi ConPurse \ (nextSeqNo): everywhere equal but nextSeqNo
    private val xiConPurseIncrease = function(
        command = { before: ConPurse, after: ConPurse ->
            before.balance == after.balance &&
                before.exLog == after.exLog &&
                before.name == after.name &&
                //before.nextSeqNo == after.nextSeqNo &&
                before.pdAuth == after.pdAuth &&
                before.status == after.status
        }
    )

    //@4paper - Kazuki transform function is akin to Z/VDM \mu-operator on \theta-expr/mk_records.
    val increasePurseOkay = function(
        command = { _: Message ->
            // Needs explicit casting on Bottom to avoid signature narrowing to Bottom (instead of Message)
            mk_(old.transform(nextSeqNo = old.nextSeqNo + 1U), Bottom as Message)
        },
        // ZEVES-PRG126 Theorem 8.5
        pre = { _ -> true },
        post = { _: Message, result ->
            val (dash, mr) = result
            xiConPurseIncrease(old, dash) &&
            dash.nextSeqNo >= old.nextSeqNo &&
                mr == Bottom
        }
    )

    // Xi ConPurse \ (nextSeqNo, exLog, pdAuth, status)
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

    //@4paper - ZEVES-PRG126 didn't need to handle purse bootstraping / pdAuth validity.
    // * pdAuth only called in PRG126 here if status is epv or epa, which the property guarantees pdAuth is non-null
    val logIfNecessary = function(
        command = {
            old.transform(exLog = old.exLog +
                (if (old.status in mk_Set(Status.epv, Status.epa)) mk_Set(old.pdAuth) else mk_Set()))
        },
        pre = { true },
        post = { dash ->
            dash.exLog == old.exLog +
                (if (old.status in mk_Set(Status.epv, Status.epa)) mk_Set(old.pdAuth) else mk_Set())
        }
    )

    // PRG126 abort is responsible for aborting an incomplete transaction (possibly logging it) before starting something else
    val abortPurseOkay = function<Message, Tuple2<ConPurse, Message>>(
        command = { _: Message ->
            val dash = logIfNecessary()
            mk_(dash.transform(
                // The Z allows for no nextSeqNo update
                //  nextSeqNo = old.nextSeqNo + 1U,
                    status = Status.eaFrom,
                // The Z doesn't say anything about what the result m! should be! Simply choosing worse (`forged`) one
                ), Bottom as Message)
        },
        //@4paper - (trivial) miminum name space (hidden) requirement in PRG126 discovered by ZEVES-PRG126
        // Z implicit prea, aee ZEVES-PRG126 Table 8.2 p.86
        // * ZEVES-PRG126 Theorem 8.6: issue on at least two names being needed (AbortPurseOkay is not total as PRG126 claims);
        // * ConPurse.nameLogged() invariant, which is the justification for this precondition discovered
        // * Because of the underdefinedness of pdAuth (PRG126 Sect. 4.8.2), its validity (ConPurse Invariants) is
        //   the responsibility of caller;
        // * given the possible null-ness of pdAuth, only check it when needed by logIfNEcessary (i.e. epv, epa)
        pre = { _ ->
            // ZEVES-PRG126 Theorem 8.6 variant because of pdAuth being possibly null
            logIfNecessary.pre() &&
                (old.status in setOf(Status.epv, Status.epa)) implies {
                    old.name in setOf(old.properties.pdAuth.from, old.properties.pdAuth.to) }
        },
        post = { _, result ->
            val (dash, mr) = result
            xiConPurseAbort(old, dash) &&
                dash.nextSeqNo >= old.nextSeqNo &&
                mr == Bottom &&
                logIfNecessary.post(dash)
        }
    )

    // PRG126 p.31 schema is an explicit user-defined precondition
    val validStartFrom = function(
        command = { _: Message, _: CounterPartyDetails -> old },
        pre = { m, cpd ->
            is_StartFrom(m) &&
                as_StartFrom(m).cpd == cpd &&
                cpd.name != old.name &&
                cpd.value <= old.balance
        },
        post = { _, _, result -> old == result }
    )

    // Xi ConPurse \ (nextSeqNo, pdAuth, status)
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

    private val increamentSeqNo = function(
        command = { n: nat -> n + 1UL },
        pre = { n -> n < MAX_NAT },
        post = { n, result ->
            //TODO Kotlin suggests `result in (n + 1UL)..MAX_NAT`; I find this clearer
            n < result && result <= MAX_NAT
        }
    )

    val startFromPurseEaFromOkay = function(
        command = { m: Message, cpd: CounterPartyDetails ->
            val dash = validStartFrom(m, cpd)
            // see PRG126, p.31 \mu-expr in StartFromPurseEafromOkay the cpd is the `to` purse of interest
            mk_(dash.transform(
                        nextSeqNo = increamentSeqNo(old.nextSeqNo),
                        pdAuth = mk_PayDetails(
                            from = old.name,
                            to = cpd.name,
                            value = cpd.value,
                            fromSeqNo = old.nextSeqNo,
                            toSeqNo = cpd.nextSeqNo,
                        ),
                        status = Status.epr,
                ),
                Bottom as Message
            )
        },
        //@4paper - ZEVES-PRG126 missing criteria discovery, see Table 8.2; same for startToPurseEaFromOkay
        // Z ConPurse is finite. CounterPartyDetails (and PayDetails) have `value: nat` that creates infinitely
        // many possible payments, which would not satisfy the finiteness of ConPurse Mapping type in Z (\ffun),
        //
        // In the Z/Eves proof, this required a underdefined upper bound, namely some `MAX\_NAT: nat`, which is
        // left unspecified, but exists, and sequence numbers must not go beyond it. This appears in the pre below.
        // This is one of the errors in the Mondex proof discovered during the Z/Eves proof. We need a similar concept
        pre = { m, cpd ->
            // ZEVES-PRG126 Theorem 8.14
            validStartFrom.pre(m, cpd) &&
                old.status == Status.eaFrom &&
                old.nextSeqNo < MAX_NAT
        },
        post = { _, cpd, result ->
            val (dash, mr) = result
            xiConPurseStart(old, dash) &&
                dash.nextSeqNo > old.nextSeqNo &&
                dash.nextSeqNo <= MAX_NAT &&
                dash.status == Status.epr &&
                // Note here the check is from(old)-to(cpd)
                dash.pdAuth != null &&
                dash.properties.pdAuth.from == old.name &&
                dash.properties.pdAuth.to == cpd.name &&
                dash.properties.pdAuth.value == cpd.value &&
                dash.properties.pdAuth.fromSeqNo == old.nextSeqNo &&
                dash.properties.pdAuth.toSeqNo == cpd.nextSeqNo &&
                mr == Bottom
        }
    )

    // In Z, startFromPurseOkay \defs abortPurseOkay \semi (startFromPurseEaFromOkay \hide (cpd))
    //    * Hiding has higher precedence than composition, so the precondition here establishes the existence of a cpd
    //    * cpd is hidden because it is implicitly aligned with the message in ValidStartFrom (e.g. cpd = as_StartFrom(m).cpd)
    //    * The way "aborting" works is that aborts run, but if nothing is to be logged, then aborting failed (i.e. no abort)
    //    * abort them must establish the pre of startFromPurseEaFromOkay, which can then execute; further pres are needed
    //      for hiding of cpd
    //TODO LF - use here similar composition created for ConWorld
    val startFromPurseOkay = function(
        command = { m: Message ->
            val (dash, _) = abortPurseOkay(m)
            dash.functions.startFromPurseEaFromOkay(m, as_StartFrom(m).cpd)
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.16 variant
            validStartFrom.pre(m, as_StartFrom(m).cpd) &&
                abortPurseOkay.pre(m) &&
                old.nextSeqNo < MAX_NAT
        },
        post = { m, result ->
            // abortPurseOkay \semi (startFromPurseEaFromOkay \hide (cpd))
            // = [Z]
            // exists ConPurse_0 & abortPurseOkay[ConPurse_0/ConPurse'] and
            //   (exists cpd: CounterPartyDetails & startFromPurseEaFromOkay[ConPurse_0/ConPurse])
            // = [in KSpec]
            // middle = old.abordPurseOkay(m) and (exists cpd & middle.startFromPurseEaFromOkay(m, cpd, dash)
            //TODO LF really need a composition operator to sort these out. Very error prone
            val (middle, mm) = old.functions.abortPurseOkay(m)
            val (dash, mr) = result
            // check abort post from start to middle; check startFrom post from middle to dash
            middle.functions.abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.startFromPurseEaFromOkay.post(m, as_StartFrom(m).cpd, result)
        }
    )

    // PRG126 p.32 schema is an explicit user-defined precondition
    val validStartTo = function(
        command = { _: Message, _: CounterPartyDetails -> old },
        pre = { m, cpd ->
            is_StartTo(m) &&
                m == mk_StartTo(cpd) &&
                cpd.name != old.name
        },
        post = { _, _, result -> old == result }
    )

    //@4paper - NEW PROTOCOL ISSUE? PRG126 p.33 says `move to epr stage` after `startTo` x typo?
    // * Z moves to `epv`: this forces the protocol path to log the message on next (Req or Val)
    // * Text says `epr`: this would not log information (logIfNecessary at the abortPurseOkay part of startTo);
    // * So either Z spec is right (and need a log) or Z is wrong and log is compromised?
    //TODO LF EAC - create to explore the protocol path !!!
    val startToPurseEaFromOkay = function(
        command = { m: Message, cpd: CounterPartyDetails ->
            val dash = validStartTo(m, cpd)
            // see PRG126, p.32 \mu-expr in StartToPurseEafromOkay the cpd is the `to` purse of interest
            val newPdAuth = mk_PayDetails(
                to = old.name,
                from = cpd.name,
                value = cpd.value,
                toSeqNo = old.nextSeqNo,
                fromSeqNo = cpd.nextSeqNo,
            )
            mk_(dash.transform(
                nextSeqNo = increamentSeqNo(old.nextSeqNo),
                status = Status.epv,
                pdAuth = newPdAuth
                ),
                mk_Req(newPdAuth) as Message
            )
        },
        pre = { m, cpd ->
            // ZEVES-PRG126 Theorem 8.15
            validStartTo.pre(m, cpd) &&
                old.status == Status.eaFrom &&
                old.nextSeqNo < MAX_NAT
              //&&
                 // cpd.nextSeqNo < old.nextSeqNo
        },
        post = { _, cpd, result ->
            val (dash, mr) = result
            xiConPurseStart(old, dash) &&
                dash.nextSeqNo > old.nextSeqNo &&
                dash.nextSeqNo <= MAX_NAT &&
                dash.status == Status.epv &&
                // Note here the check is to(old)-from(cpd)
                dash.pdAuth != null &&
                dash.properties.pdAuth.to == old.name &&
                dash.properties.pdAuth.from == cpd.name &&
                dash.properties.pdAuth.value == cpd.value &&
                dash.properties.pdAuth.toSeqNo == old.nextSeqNo &&
                dash.properties.pdAuth.fromSeqNo == cpd.nextSeqNo &&
                mr == mk_Req(dash.properties.pdAuth)
        }
    )

    val startToPurseOkay = function(
        command = { m: Message ->
            val (dash, _) = abortPurseOkay(m)
            dash.functions.startToPurseEaFromOkay(m, as_StartTo(m).cpd)
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.17 variant
            validStartTo.pre(m, as_StartTo(m).cpd) &&
                //as_StartTo(m).cpd.nextSeqNo < old.nextSeqNo &&
                abortPurseOkay.pre(m) &&
                old.nextSeqNo < MAX_NAT
        },
//        post = { m, result ->
//            val (middle, mm) = old.functions.abortPurseOkay(m)
//            val (dash, mr) = result
//            middle.functions.abortPurseOkay.post(m, mk_(middle, mm)) &&
//                middle.functions.startToPurseEaFromOkay.post(m, as_StartTo(m).cpd, result)
//        }
    )

    // PRG126 p.33 schema is an explicit user-defined precondition
    val authenticReqMessage = function(
        command = { _: Message -> old },
        pre = { m ->
            //TODO LF EAC - maybe remove the status check here
            old.status != Status.eaFrom &&
                m == mk_Req(old.properties.pdAuth) },
        post = { _, result -> old == result }
    )

    // Xi ConPurse \ (balance, status)
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

    //@4paper - PRG126 nuance on promoting messages to the ether for later checking; Mondex bug?!
    // * startFromPurseEaFromOkay does not return a `req pdAuth` (but `bottom`) message; `startToPurseEaFromOkay` does!
    // * on promotion in PhiBop: startFrom (bottom) + startTo (req pdAuth) mrddshrd on the ether
    // * Z says: startFrom.status' = epr + m! = bottom, startTo.status' = epv + m! = `req pdAuth`(!)?
    // * Z reqPurseOkay expects: status  = epr && m? = req pdAuth on call but:
    //      - startFrom returns: status' = epr && m! = bottom (!!!)
    //      - startTo returns  : status' = epv && m! = req pdAuth
    // * How is it that the PRG126 p. 53: `complete protocol = StartFrom ; StartTo ; Req ; Val; Ack` handles \semi mismatch?
    //      - the complex abort mechanisms (e.g. at the ConWorld x ConPurse levels) blurs the issue a bit: bug or hidden complexity?
    // * Successful path only, p.49: ConWorld.startFromEaFromOkay:
    //      - (\exists \Delta ConPurse & \Phi BOp \land startFromPurseEaFromOkay)
    //      - startFromPurseOkay(m?) = abortPurseOkay \semi (startFromPurseEaFromOkay \hides(cpd))
    //          = logging is non-deterministic, given conpurse.status can be anything (no pre); if epv/epa, logs
    //          = overall result: m! = bottom && status' = epr && pdAuth' = from(purse)-to(m?.cpd)
    // * Successful parth only, ConWorld.startToEaFromOkay:
    //      - (\exists \Delta ConPurse & \Phi BOp \land startToPurseOkay)
    //      - startToPurseOkay(m?) = abortPurseOkay \semi (startToPurseEaFromOkay \hides(cpd))
    //          = if called right after startFrom finishes, then abortPurseOkay will:
    //              + perform no logging given status = epr (!)
    // ---BUG?--->> + coming `pdAuth = from(purse)-to(m?.cpd)` won't be logged ?!! <<---BUG?---
    //          = overall result: m! = req pdAuth' && status' = epv && pdAuth' = to(purse)-from(m?.cpd)
    //      - reqPurseOkay(m?) has no abort
    //          = if called right after startFrom
    //              + will succeed: right status = epr, but will miss logging the from(pdAuth)?
    //          = if called right after startTo
    //              + will fail: wrong status = epv, but right message to(pdAuth)?
    // * This is either a real bug or gets resolved in the complicated promotion setup with BetweenWorld constraints:
    //      - See ConWorld.kt table on what the convoluted naming is
    //      - See BetweenWorld.kt invariants B1-16 on what the expected message exchanges are
    //TODO LF EAC - write for this situation; something is off here
    val reqPurseOkay = function(
        command = { m: Message ->
            val dash = authenticReqMessage(m)
            mk_(dash.transform(
                balance = old.balance - old.properties.pdAuth.value,
                status = Status.epa
            ),
                mk_Val(old.properties.pdAuth) as Message
            )
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.7
            old.status == Status.epr &&
                authenticReqMessage.pre(m)
        },
        post = { _, result ->
            val (dash, mr) = result
            xiConPurseReq(old, dash) &&
                dash.balance == old.balance - old.properties.pdAuth.value &&
                dash.status == Status.epa &&
                mr == mk_Val(old.properties.pdAuth)
        }
    )

    // PRG126 p.34 schema is an explicit user-defined precondition
    val authenticValMessage = function(
        command = { _: Message -> old },
        pre = { m ->
            //TODO LF - maybe remove the status check here
            old.status != Status.eaFrom &&
                m == mk_Val(old.properties.pdAuth) },
        post = { _, result -> old == result }
    )

    // Xi ConPurse \ (balance, status)
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

    //TODO LF EAC - eaTo status, Fig 5.1 check?
    val valPurseOkay = function(
        command = { m: Message ->
            val dash = authenticValMessage(m)
            mk_(dash.transform(
                balance = old.balance + old.properties.pdAuth.value,
                status = Status.eaTo
            ), mk_Ack(old.properties.pdAuth))
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.8
            old.status == Status.epv &&
                authenticValMessage.pre(m)
        },
        post = { _, result ->
            val (dash, mr) = result
            xiConPurseVal(old, dash) &&
                dash.balance == old.balance + old.properties.pdAuth.value &&
                dash.status == Status.eaTo &&
                mr == mk_Ack(old.properties.pdAuth)
        }
    )

    // PRG126 p.34 schema is an explicit user-defined precondition
    val authenticAckMessage = function(
        command = { _: Message -> old },
        pre = { m ->
            //TODO LF - maybe remove the status check here
            old.status != Status.eaFrom &&
                m == mk_Ack(old.properties.pdAuth) },
        post = { _, result -> old == result }
    )

    // Xi ConPurse \ (status, pdAuth)
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
            mk_(dash.transform(
                status = Status.eaFrom
            ), Bottom as Message)
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.9
            old.status == Status.epa &&
                authenticAckMessage.pre(m)
        },
        post = { _, result ->
            val (dash, mr) = result
            xiConPurseAck(old, dash) &&
                dash.status == Status.eaFrom &&
                mr == Bottom
        }
    )

    val readExceptionLogPurseEaFromOkay = function(
        command = { m: Message ->
            // PRG126 allows for non-deterministic reponse, prefer an exception log, if one exists
            val mr = if (old.exLog.isEmpty()) Bottom
                else mk_ExceptionLogResult(old.name, old.exLog.arbitrary())
            mk_(old, mr)
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.10
            old.status == Status.eaFrom && m == ReadExceptionLog
        },
        post = { _, result ->
            val (dash, mr) = result
            xiConPurse(old, dash) && (
                mr == Bottom || mr in dash.exLog.map { ld -> mk_ExceptionLogResult(dash.name, ld) })
        }
    )

    val readExceptionLogPurseOkay = function(
        command = { m: Message ->
            val (dash, _) = abortPurseOkay(m)
            dash.functions.readExceptionLogPurseEaFromOkay(m)
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.12 variant
            m == ReadExceptionLog &&
                old.functions.abortPurseOkay.pre(m)
        },
        post = { m, result ->
            val (middle, mm) = abortPurseOkay(m)
            val (dash, mr) = result
            abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.readExceptionLogPurseEaFromOkay.post(m, result)
        }
    )

    // Xi ConPurse \ (exLog)
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
        command = { _: Message ->
            mk_(old.transform(
                exLog = emptySet()
            ), Bottom as Message
            )
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.11
            old.status == Status.eaFrom &&
                old.exLog.isNotEmpty() &&
                m == mk_ExceptionLogClear(old.name,mk_Clear(as_Set1(old.exLog)))
        },
        post = { _, result ->
            val (dash, mr) = result
            xiConPurseClear(old, dash) &&
                dash.exLog.isEmpty() &&
                mr == Bottom
        }
    )

    val clearExceptionLogPurseOkay = function(
        command = { m: Message ->
            val (dash, _) = abortPurseOkay(m)
            dash.functions.clearExceptionLogPurseEaFromOkay(m)
        },
        pre = { m ->
            // ZEVES-PRG126 Theorem 8.13 variant
            old.functions.abortPurseOkay.pre(m) &&
                old.exLog.isNotEmpty() &&
                m == mk_ExceptionLogClear(old.name,
                mk_Clear(
                    as_Set1(
                    old.exLog +
                            (if (old.status in mk_Set(Status.epv, Status.epa)) mk_Set(old.pdAuth) else emptySet())
                          )
                        )
                    )
        },
        post = { m, result ->
            val (middle, mm) = abortPurseOkay(m)
            val (dash, mr) = result
            abortPurseOkay.post(m, mk_(middle, mm)) &&
                middle.functions.clearExceptionLogPurseEaFromOkay.post(m, result)
        }
    )
}
