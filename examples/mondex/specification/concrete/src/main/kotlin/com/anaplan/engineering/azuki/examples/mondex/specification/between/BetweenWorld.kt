package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.as_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.is_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.mk_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.AuxWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Clear_Module.mk_Clear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear_Module.as_ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear_Module.is_ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear_Module.mk_ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.as_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.is_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.mk_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.as_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.is_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req_Module.mk_Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.as_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.is_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.as_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.is_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.as_Val
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.is_Val
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val_Module.mk_Val
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.MAX_NAT
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.azuki.examples.mondex.specification.pairwise_disjoint
import com.anaplan.engineering.kazuki.core.*
import kotlin.random.Random

@Module
interface BetweenWorld : AuxWorld {

    @FunctionProvider(BetweenWorldSeed::class)
    val choice: BetweenWorldSeed

    @FunctionProvider(BetweenWorldFunctions::class)
    val functions: BetweenWorldFunctions

    // PRG126 talks about `future` (logs + messages) constraints on the ConWorld: B1-16 (copying PRG's doc for reference)
    // ZEVES-PRG126 talks about missing (or implicit) constraints on the ConWorld: B3.1-2; etc
    // These constraints depend on the ether or the conAuthPurse, so will limit search to them rather than open on PayDetails

    /**
     *   All $req$ messages in the $ether$ refer to authentic $to$ purses
     */
    @Invariant
    fun b1_etherReqMsgAuthentic() =
        //\forall pd: PayDetails | req(pd) \in ether & pd \in authenticTo ....
        forall(ether.filter { is_Req(it) }) { as_Req(it).pd in properties.authenticTo }

    /**
     *   There are no `future' $req$ messages: all $req$
     *   messages in the $ether$ hold a $to$ purse sequence number less than
     *   that purse's next sequence number.  (It puts no constraint on the
     *   $from$ purse's sequence number, because the $from$ purse mentioned
     *   in a $req$ message need not have started the transaction yet, and
     *   need not even be authentic.)
     */
    @Invariant
    fun b2_etherNoFutureToPurseReqMsg() =
        //\forall pd: PayDetails | req(pd) \in ether & pd.toSeqNo < conAuthPurse(pd.to).nextSeqNo
        forall(ether.filter { is_Req(it) }) { m ->
            as_Req(m).pd.let { it.toSeqNo < conAuthPurse[it.to].nextSeqNo } }

    /**
     *  ZEVES-PRG126 addition: original purse was missing additional information about
     *  the authenticity of val messages in the ether for to and from purses!
     */
    @Invariant
    fun b3_1_2_etherValMsgAuthentic() =

        forall(ether.filter { is_Val(it) }) { m ->
            as_Val(m).pd in properties.authenticTo inter properties.authenticFrom
        }

    /**
     *   There are no `future' $val$ messages: all $val$ messages in the $ether$
     *   hold a $to$ purse sequence number less than that purse's next
     *   sequence number and a $from$ purse sequence number less than that
     *   purse's next sequence number.
     */
    @Invariant
    fun b3_etherNoFutureToPurseValMsg() =
        //\forall pd: PayDetails | val(pd) \in ether & pd.toSeqNo < conAuthPurse(pd.to).nextSeqNo \land pd.fromSeqNo < conAuthPurse(pd.from).nextSeqNo
        b3_1_2_etherValMsgAuthentic() &&
            forall(ether.filter { is_Val(it) }) { m ->
                as_Val(m).pd.let {
                    it.toSeqNo < conAuthPurse[it.to].nextSeqNo &&
                        it.fromSeqNo < conAuthPurse[it.to].nextSeqNo
                }
            }

    /**
     *  ZEVES-PRG126 addition: original purse was missing additional information about
     *  the authenticity of ack messages in the ether for to and from purses!
     */
    @Invariant
    fun b4_1_2_etherAckMsgAuthentic() =
        forall(ether.filter { is_Ack(it) }) { m ->
            as_Ack(m).pd in properties.authenticTo inter properties.authenticFrom
        }

    /**
     *   There are no `future' $ack$ messages: all $ack$ messages in the $ether$
     *   hold a $to$ purse sequence number less than that purse's next
     *   sequence number and a $from$ purse sequence number less than that
     *   purse's next sequence number.
     */
    @Invariant
    fun b4_etherNoFutureToPurseAckMsg() =
        //\forall pd: PayDetails | ack(pd) \in ether & pd.toSeqNo < conAuthPurse(pd.to).nextSeqNo \land pd.fromSeqNo < conAuthPurse(pd.from).nextSeqNo
        b3_1_2_etherValMsgAuthentic() &&
            forall(ether.filter { is_Val(it) }) { m ->
                as_Val(m).pd.let {
                    it.toSeqNo < conAuthPurse[it.to].nextSeqNo &&
                        it.fromSeqNo < conAuthPurse[it.to].nextSeqNo
                }
            }

    /**
     * There are no `future' $from$ logs based on the $nextSeqNo$ of the $from$ purse
     */
    @Invariant
    fun b5_noFutureFromLogged() =
        forall(properties.fromLogged) { pd -> pd.fromSeqNo < conAuthPurse[pd.from].nextSeqNo }

    /**
     * There are no `future' $to$ logs based on the $nextSeqNo$ of the $to$ purse
     */
    @Invariant
    fun b6_noFutureToLogged() =
        forall(properties.toLogged) { pd -> pd.toSeqNo < conAuthPurse[pd.to].nextSeqNo }

    //@4paper - this relate to the potential bug found
    //TODO LF EAC create one to exercise the issue on ConWorld.reqPurseOkay
    /**
     *   There are no `future' $from$ logs based on the $pdAuth.fromSeqNo$ of a purse in $epr$ or $epa$:
     *   all $from$ logs refer only to past $from$ transactions. So all $from$ logs referring to a
     *   purse that is currently in a transaction as a $from$ purse (that is, in $epr$ or $epa$), hold
     *   a $from$ sequence number strictly less than that purse's stored current transaction sequence number.
     */
    @Invariant
    fun b7_noFutureEprEpaFromLogged() =
        forall(properties.fromLogged.filter { pd ->
            conAuthPurse[pd.from].status in mk_Set(Status.epr, Status.epa) } ) {
            it.fromSeqNo < conAuthPurse[it.from].properties.pdAuth.fromSeqNo
        }

    //@4paper - this relate to the potential bug found
    //TODO LF EAC create one to exercise the issue on ConWorld.reqPurseOkay
    /**
     * There are no `future' $to$ logs based on the pdAuth.toSeqNo of a purse in $epv$ or $eaTo$
     *   all $to$ logs refer only to past $to$ transactions.  So all $to$
     *   logs referring to a purse that is currently in a transaction as a
     *   $to$ purse (in $epv$), hold a $to$ sequence number strictly less
     *   than that purse's stored current transaction sequence number.
     */
    @Invariant
    fun b8_noFutureEpvEaToToLogged() =
        forall(properties.toLogged.filter { pd ->
            conAuthPurse[pd.to].status in mk_Set(Status.epv, Status.eaTo) } ) {
            it.toSeqNo < conAuthPurse[it.to].properties.pdAuth.toSeqNo
        }

    /**
     * If the $from$ purse is in $epr$ then there is no $val$ message or $ack$ message in the $ether$.
     */
    @Invariant
    fun b9_eprDisjointFromValAckEther() =
        forall(properties.fromInEpr) { pd ->
            mk_Seq(mk_Set(mk_Val(pd), mk_Ack(pd)), ether).pairwise_disjoint()
        }

    /**
     * There is a $req$ message but no $ack$ message in the $ether$ precisely when the $to$ purse is in $epv$ or
     * has logged the transaction
     */
    @Invariant
    fun b10_reqAckEtherEpvToLoggedIff() =
        // For all + iff in two parts
        // \forall pd: PayDetails & (req(pd) \in ether \land ack(pd) !in ether)\iff (pd \in toInEpv \union toLogged)
        forall(properties.toInEpv inter properties.toLogged) { pd -> mk_Req(pd) in ether && mk_Ack(pd) !in ether } &&
            forall(ether.filter { pd -> is_Req(pd) && !is_Ack(pd) }) { m ->
                as_Req(m).pd in (properties.toInEpv + properties.toLogged)
            }

    /**
     * If the $to$ purse is in $epv$ and
     *   there is a $val$ message in the $ether$, then either the $from$
     *   purse is in $epa$ or has logged the transaction
     */
    @Invariant
    fun b11_toPurseEpvValLoggedFrom() =
        forall(ether.filter { is_Val(it) && as_Val(it).pd in properties.toInEpv }) { m ->
            as_Val(m).pd in (properties.fromInEpa + properties.fromLogged)
        }

    /**
     * If the $from$ purse is in $epa$ or has
     *   logged the transaction, then there is a $req$ in the $ether$
     */
    //TODO LF EAC this chaining of b11-12 seems related to the possible Mondex bug?
    @Invariant
    fun b12_epaFromLoggedReqEther() =
        forall(properties.fromInEpa + properties.fromLogged) { pd -> mk_Req(pd) in ether }

    /**
     * The set $toLogged$ is finite.  This is
     *   sufficient to ensure that $definitelyLost$ is finite
     */
    // B13 is trivial because we are dealing with finite sets already; ignore
    @Invariant
    fun b13_toLoggedFinite() = true // toLogged \in \finset~PayDetails

    /**
     * Log result messages are logged.  The
     *   log details of any $exceptionLogResult$ message in the ether is
     *   either archived or in a purse transaction exception log
     */
    @Invariant
    fun b14_logArchiveConsistency() =
        forall(ether.filter { is_ExceptionLogResult(it) }) { m ->
            as_ExceptionLogResult(m).let { mk_(it.name, it.pd) in properties.allLogs } }

    /**
     * Exception log clear messages refer only to archived logs
     */
    @Invariant
    fun b15_allArchivedAreClearMsgs() =
        forall(ether.filter { is_ExceptionLogClear(it) }) { m ->
            as_ExceptionLogClear(m).let { (it.name x it.clear.pds) subset archive } }

    /**
     * For each $PayDetails$ in the logs there
     *   is a corresponding $PayDetails$ in a $req$ message in the ether
     */
    @Invariant
    fun b16_reqPayDetailsLogged() =
        forall(properties.fromLogged + properties.toLogged) { pd -> mk_Req(pd) in ether }
}

const val SEED_CHOICE = 137L
enum class UnprotectedPromotionChoice { Ignore, Abort, Act }
enum class ProtectedPromotionChoice { Ignore, Act }

typealias PurseOp = VFunction1<Message, Tuple2<ConPurse, Message>>

class BetweenWorldSeed(private val old: BetweenWorld) {
    val seed by property { Random(SEED_CHOICE) }
    //TODO LF make it non deterministic afterwards
    fun nextUnprotected(): UnprotectedPromotionChoice = UnprotectedPromotionChoice.Act
        // UnprotectedPromotionChoice.entries.random(seed)
    fun nextProtected(): ProtectedPromotionChoice = ProtectedPromotionChoice.Act
        // ProtectedPromotionChoice.entries.random(seed)
}

class BetweenWorldFunctions(private val old: BetweenWorld) {

    fun xiBetweenWorld(before: BetweenWorld, after: BetweenWorld) =
        before.conAuthPurse == after.conAuthPurse &&
            before.ether == after.ether &&
            before.archive == after.archive

    // Promotion will receive the purse-level operation to call
    val phiBOp = function(
        command = { name: Name, m: Message, act: PurseOp ->
            val (cdash, mbang) = act(m)
            mk_(old.transform(
                        conAuthPurse = old.conAuthPurse * mk_(name, cdash),
                        ether = old.ether + mk_Set(mbang),
                ),
                mbang)
        },
        // ZEVES-PRG126 Schema 8.17 p.71, Table 8.3, p.87
        pre = { name, m, act ->
            m in old.ether && name in old.conAuthPurse.dom && act.pre(m)
            // \theta ConPurse = conAuthPurse name? is implicit here
        },
        post = { name, m, act, result ->
            val (dash, mbang) = result
            // Somewhat redundant check but kept to safeguard promotion manual setup
            act.post(m, mk_(dash.conAuthPurse[name], mbang)) &&
                dash.conAuthPurse == old.conAuthPurse * mk_(name, dash.conAuthPurse[name]) &&
                dash.archive == old.archive &&
                dash.ether == old.ether + mk_Set(mbang)
        }
    )

    val ignore = function(
        command = { _: Name, _: Message ->
            mk_(old, Bottom as Message)
        },
        // ZEVES-PRG126 Theorem 8.18
        pre = { _, _ -> true },
        post = { _, _, result ->
            val (dash, mbang) = result
            xiBetweenWorld(old, dash)
            mbang == Bottom
        }
    )

    val increase = function(
        command = { name: Name, m: Message ->
            val choice = old.choice.nextUnprotected()
            if (choice == UnprotectedPromotionChoice.Act)
                mk_(choice, old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.increasePurseOkay))
            else
                mk_(choice, old.functions.ignore(name, m))
        },
        // ZEVES-PRG126 Theorem 8.19
        // ZEVES-PRG126 Theorem 8.31 consider the most strict pre?
        // Check all choices, no short-circuiting; post, check any (or specific choice returned?)
        pre = { name, m ->
            val ignorePre = old.functions.ignore.pre(name, m)
            val actPre = old.functions.phiBOp.pre(name, m, old.conAuthPurse[name].functions.increasePurseOkay)
            setOf(ignorePre, actPre).all { it }&&
                Bottom in old.ether
        },
//        post = { name, m, result ->
//            //val (choice, (dash, mbang)) = result
//            val (choice, r) = result
//            val (dash, mbang) = r
//            if (choice == UnprotectedPromotionChoice.Act)
//                dash.functions.phiBOp.post(name, m, dash.conAuthPurse[name].functions.increasePurseOkay, mk_(dash, mbang))
//            else
//                dash.functions.ignore.post(name, m, mk_(dash, mbang))
//        }
    )

    // Important to reduce non-determinism (i.e. Mondex Abort disjoins with Ignore; Ignore is disjoined with Abort elsewhere again)
    val abortOnly = function(
        command = { name: Name, m: Message ->
            old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.abortPurseOkay)
        },
        // ZEVES-PRG126 Theorem 8.20, Schema 8.18, Theorem 8.32
        pre = { name, m ->
            Bottom in old.ether &&
                old.functions.phiBOp.pre(name, m, old.conAuthPurse[name].functions.abortPurseOkay)
            //TODO LF Add Schema 8.18 clauses!
        },
//        post = { name, m, result ->
//            val (dash, mbang) = result
//            mbang == Bottom &&
//                dash.functions.phiBOp.post(name, m, dash.conAuthPurse[name].functions.abortPurseOkay, result)
//        }
    )

    val abort = function(
        command = { name: Name, m: Message ->
            val choice = old.choice.nextUnprotected()
            if (choice == UnprotectedPromotionChoice.Act)
                mk_(choice, old.functions.abortOnly(name, m))
            else
                mk_(choice, old.functions.ignore(name, m))
        },
        // ZEVES-PRG126 Theorem 8.20, Schema 8.18, Theorem 8.32
        pre = { name, m ->
            val ignorePre = old.functions.ignore.pre(name, m)
            val abortPre = old.functions.abortOnly.pre(name, m)
            // No short circuiting!
            setOf(ignorePre, abortPre).all { it }
        },
//        post = { name, m, result ->
//            val (choice, r) = result
//            val (dash, mbang) = r
//            mbang == Bottom &&
//                (if (choice == UnprotectedPromotionChoice.Act)
//                    dash.functions.abortOnly.post(name, m, mk_(dash, mbang))
//                else
//                    dash.functions.ignore.post(name, m, r))
//        }
    )

    val startFrom = function(
        command = { name: Name, m: Message ->
            val choice = old.choice.nextUnprotected()
            val r = when (choice) {
                UnprotectedPromotionChoice.Ignore -> old.functions.ignore(name, m)
                UnprotectedPromotionChoice.Abort  -> old.functions.abortOnly(name, m)
                UnprotectedPromotionChoice.Act    -> old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.startFromPurseOkay)
            }
            mk_(choice, r)
        },
        // ZEVES-PRG126 Table 8.3, p.87, Schema 8.19 p.73, Theorem 8.34
        pre = { name, m  ->
            //Proof engineering note from ZEVES-PRG126 on PhiBOp:
            // * name? \in \dom conAuthPurse, \theta ConPurse = conAutPurse(name?)
            // * Thus, (\theta ConPurse).name = name? (!!!)
            // * Schema 8.19 says: (\theta ConPurse).name \in \dom conAuthPurse \implies (\theta ConPurse).name \neq name?
            // * which is logically: (\theta ConPurse).name = name? \implies \lnot (\theta ConPurse).name \in \dom conAuthPurse
            // * Thus, it is just for proof engineering (i.e. impose injectivity property explicitly)?
            // * Seems so. Lemma 8.26 + 8.27, we see the between world \oplus for name? x name/from
            //@4paper - proof engineering got too involved/confused due to lack of concrete running example?!
            //@4paper - double checking pres is not onerous on execution time
            val purseName = old.conAuthPurse[name].name
            val ignorePre = old.functions.ignore.pre(name, m)
            val abortPre = old.functions.abortOnly.pre(name, m)
            val startFromOkayPre = old.functions.phiBOp.pre(name, m, old.conAuthPurse[name].functions.startFromPurseOkay)
                //old.conAuthPurse[name].functions.startFromPurseEaFromOkay)
            //@4paper - proof engineering checks don't need to feature; repeated checks aren't expensive to run
            val startFromPre =
                old.conAuthPurse[name].status == Status.eaFrom &&
                    old.conAuthPurse[name].nextSeqNo < MAX_NAT &&
                    is_StartFrom(m) &&
                    Bottom in old.ether &&
                    payDetailsFromWithinNextSeqNo(name, purseName) &&
                    // Removed, given argument against Lemma 8.26/27 here
                    //((purseName in old.conAuthPurse.dom) implies { purseName != name }) &&
                    freshStartFromConPurse(purseName, as_StartFrom(m))
            setOf(ignorePre, abortPre, startFromOkayPre, startFromPre).all { it }
        },
        //@4paper - manual proofs always took the abort path; ZEVES-PRG126 Ch8 shows the complexity of the consequences
//        post = { name, m, result ->
//            val (choice, r) = result
//            val (dash, mbang) = r
//            when (choice) {
//                UnprotectedPromotionChoice.Ignore -> old.functions.ignore.post(name, m, r)
//                UnprotectedPromotionChoice.Abort  -> old.functions.abortOnly.post(name, m, r)
//                UnprotectedPromotionChoice.Act    -> old.functions.phiBOp.post(name, m, old.conAuthPurse[name].functions.startFromPurseOkay, r)
//            }
//        }
    )

    val startTo = function(
        command = { name: Name, m: Message ->
            val choice = old.choice.nextUnprotected()
            val r = when (choice) {
                UnprotectedPromotionChoice.Ignore -> old.functions.ignore(name, m)
                UnprotectedPromotionChoice.Abort  -> old.functions.abortOnly(name, m)
                UnprotectedPromotionChoice.Act    -> old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.startToPurseOkay)
            }
            mk_(choice, r)
        },
        // ZEVES-PRG126 Table 8.3, p.87, Schema 8.20 p.74, Theorem 8.36
        pre = { name, m  ->
            val purseName = old.conAuthPurse[name].name
            val ignorePre = old.functions.ignore.pre(name, m)
            val abortPre = old.functions.abortOnly.pre(name, m)
            val startToOkayPre = old.functions.phiBOp.pre(name, m, old.conAuthPurse[name].functions.startToPurseOkay)
            val startToPre =
                is_StartTo(m) &&
                    old.conAuthPurse[name].status == Status.eaFrom &&
                    old.conAuthPurse[name].nextSeqNo < MAX_NAT &&
                    as_StartTo(m).cpd.nextSeqNo < MAX_NAT &&
                    payDetailsToWithinNextSeqNo(name, purseName) &&
                    //((purseName in old.conAuthPurse.dom) implies { purseName != name }) &&
                    freshStartToConPurse(name, as_StartTo(m))
            setOf(ignorePre, abortPre, startToOkayPre, startToPre).all { it }
        },
    )

    // req already used for the message so extended names to include op

    val reqOp = function(
        command = { name: Name, m: Message ->
            val choice = old.choice.nextProtected()
            val r = when (choice) {
                ProtectedPromotionChoice.Ignore -> old.functions.ignore(name, m)
                ProtectedPromotionChoice.Act    -> old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.reqPurseOkay)
            }
            mk_(choice, r)
        },
        //TODO EK chase the corresponding ones in Schma 8.21 onwards
        pre = { name, m ->
            val purse = old.conAuthPurse[name]
            val ignorePre = old.functions.ignore.pre(name, m)
            val reqOkayPre = old.functions.phiBOp.pre(name, m, old.conAuthPurse[name].functions.reqPurseOkay)
            // ZEVES-PRG126 Table 8.3, p.87, Schema 8.21, Theorem 8.37
            val reqPurseOkayPre =
                purse.functions.reqPurseOkay.pre(m) && true
//                (mk_Val(purse.properties.pdAuth) !in old.ether) implies {
//                    purse.properties.pdAuth.to in old.conAuthPurse.dom &&
//                        purse.properties.pdAuth.from in old.conAuthPurse.dom &&
//                        (purse.properties.pdAuth.to != name) implies {
//                            purse.properties.pdAuth.toSeqNo < old.conAuthPurse[purse.properties.pdAuth.to].nextSeqNo
//                    } &&
//                        (purse.properties.pdAuth.from != name) implies {
//                            purse.properties.pdAuth.fromSeqNo < old.conAuthPurse[purse.properties.pdAuth.from].nextSeqNo
//                    } &&
//                        old.properties.fromInEpr.all { pdIn -> pdIn != purse.properties.pdAuth } &&
//                        (mk_(purse.properties.pdAuth.from, purse.properties.pdAuth) !in old.archive) implies {
//                            purse.properties.pdAuth in old.conAuthPurse[purse.properties.pdAuth.from].exLog
//                    }
//                } &&
//                allPayDetails().filter { xEPR ->
//                    xEPR.from in old.conAuthPurse.dom &&
//                        old.conAuthPurse[xEPR.from].status == Status.epr &&
//                        old.conAuthPurse[xEPR.from].pdAuth == xEPR
//                }.any { xEPR -> xEPR.from != name } &&
//                allPayDetails().filter { pd ->
//                    pd.from in old.conAuthPurse.dom
//                }.any { pd -> pd != purse.properties.pdAuth } &&
//                purse.transform(
//                    balance = purse.balance - purse.properties.pdAuth.value,
//                    name = purse.properties.pdAuth.from,
//                    status = Status.epa
//                ) !in old.conAuthPurse.rng
//                (as_Req(m).pd.from in old.conAuthPurse.dom) implies {
//                    purse.properties.pdAuth != as_Req(m).pd
//                }
            setOf(ignorePre, reqOkayPre, reqPurseOkayPre).all { it }
        },
        //TODO no need for these checks?
        post = { name, m, result ->
            val (choice, r) = result
            val (dash, mbang) = r
            old.conAuthPurse[name].functions.reqPurseOkay.post(
                m, mk_(dash.conAuthPurse[name], mbang))
        }
    )

    val valOp = function(
        command = { name: Name, m: Message ->
            TODO("Choice for ignore of valPurseOkay. Here just valPurseOkay route completed.")
            mk_(old, mk_Ack(old.conAuthPurse[name].properties.pdAuth))
        },
        pre = { name, m ->
            val purse = old.conAuthPurse[name]
            if (old.conAuthPurse[name].pdAuth == null) {
                false
            }
            else {
                val valOkayPre = //old.functions.phiBOpFramePre(m, purse.name) &&
                    purse.functions.valPurseOkay.pre(m) &&
                    allPayDetails().filter { xEPV ->
                        xEPV.to in old.conAuthPurse.dom &&
                            old.conAuthPurse[xEPV.to].status == Status.epv &&
                            old.conAuthPurse[xEPV.to].pdAuth == xEPV
                    }.all { xEPV -> xEPV.to != name } &&
                    allPayDetails().filter { pd -> pd.to in old.conAuthPurse.dom }.any { pd -> pd != purse.properties.pdAuth } &&
                    purse.transform(
                        balance = purse.balance + purse.properties.pdAuth.value,
                        status = Status.eaTo
                    ) !in old.conAuthPurse.rng

                valOkayPre
            }
        },
        post = { name, m, result ->
            val (dash, mbang) = result
            // needs more when we have disjunction
            val valOkayPost = old.conAuthPurse[name].functions.valPurseOkay.post(
                m, mk_(dash.conAuthPurse[name], mbang)
            )

            valOkayPost
        }
    )

    val ackOp = function(
        command = { name: Name, m: Message ->
            TODO("Choice between ignore and ackPurseOkay")
            mk_(old, Bottom)
        },
        pre = { name, m ->
            val purse = old.conAuthPurse[name]
            if (purse.pdAuth == null) {
                false
            }
            else {
                val ackOkayPre = //old.functions.phiBOpFramePre(m, name) &&
                    purse.functions.ackPurseOkay.pre(m) &&
                    allPayDetails().filter { xEPA ->
                        xEPA.from in old.conAuthPurse.dom &&
                            old.conAuthPurse[xEPA.from].status == Status.epa &&
                            old.conAuthPurse[xEPA.from].pdAuth == xEPA
                    }.all { xEPA -> xEPA.from != name} &&
                    purse.transform(status = Status.eaTo) !in old.conAuthPurse.rng

                ackOkayPre
            }
        },
        post = { name, m, result ->
            val (dash, mbang) = result
            val ackOkayPost = old.conAuthPurse[name].functions.ackPurseOkay.post(
                m, mk_(dash.conAuthPurse[name], mbang)
            )

            ackOkayPost
        }
    )

    val readExceptionLog = function(
        command = { name: Name, m: Message ->
            TODO("Choice between ignore and readExceptionLogPurseOkay")
            mk_(old, Bottom)
        },
        pre = { name, m ->
            val readExceptionLogOkayPre = //old.functions.phiBOpFramePre(m, name) &&
                old.conAuthPurse[name].functions.readExceptionLogPurseEaFromOkay.pre(m) &&
                Bottom in old.ether

            readExceptionLogOkayPre
        },
        post = { name, m, result ->
            val (dash, mbang) = result
            val readExceptionLogOkayPost = old.conAuthPurse[name].functions.readExceptionLogPurseOkay.post(
                m, mk_(dash.conAuthPurse[name], mbang)
            )

            readExceptionLogOkayPost
        }
    )

    val clearExceptionLog = function(
        command = { name: Name, m: Message ->
            TODO("Choice between ignore and clearExceptionLogPurseOkay")
            mk_(old, Bottom)
        },
        pre = { name, m ->
            val purse = old.conAuthPurse[name]
            val clearExceptionLogOkayPre =// old.functions.phiBOpFramePre(m, name) &&
                purse.functions.clearExceptionLogPurseEaFromOkay.pre(m) &&
                allPayDetails().all { pd -> pd !in old.conAuthPurse[name].exLog } &&
                purse.transform(exLog = emptySet()) !in old.conAuthPurse.rng

            clearExceptionLogOkayPre
        },
        post = { name, m, result ->
            val (dash, mbang) = result
            val clearExceptionLogOkayPost = old.conAuthPurse[name].functions.clearExceptionLogPurseOkay.post(
                m, mk_(dash.conAuthPurse[name], mbang)
            )

            clearExceptionLogOkayPost
        }
    )

    val authoriseExLogClearOkay = function(
        command = { name: Name, m: Message ->
            val pds = mk_Set1<PayDetails>() // TODO(Fix pds)
            mk_(old.transform(
                ether = old.ether.plus(mk_ExceptionLogClear(name, mk_Clear(pds))),
            ), m)
        },
        pre = { name, m -> true },
        post = { name, m, result ->
            val (dash, mbang) = result
            old.conAuthPurse[name] == dash.conAuthPurse[name] &&
                dash.ether == old.ether.plus(mbang) &&
                old.archive == dash.archive &&
                TODO("The there exists segment - LF?")
        }
    )

    val authoriseExLogClear = function(
        command = { name: Name, m: Message ->
            mk_(old, m)
        },
        pre = { name, m -> true },
        post = { name, m, result ->
            val authoriseExLogClearOkay = old.functions.authoriseExLogClearOkay.post(name, m, result)

            authoriseExLogClearOkay
        }
    )

    val archive = function(
        command = { _: Name, _: Message ->
            // copies some exception log information from messages in the ether to the archive
            // @QST LF do we want some random number copied? At this point will move 0.
            mk_(old, Bottom)
        },
        pre = { _, _ ->
            true
        },
        post = { name, _, result ->
            val (dash, mbang) = result
            old.conAuthPurse[name] == dash.conAuthPurse[name] &&
                old.ether == dash.ether &&
                mbang == Bottom &&
                old.archive.subset(dash.archive) &&
                dash.archive.all { log -> log in old.archive || mk_ExceptionLogResult(log._1, log._2) in old.ether}
        }
    )

    // ZEVES-PRG126 Schema 8.19
    // StartFrom check: CP encompass a set of purses; check they are not in range of conAuthPurse (i.e. new payment request)
    // CP \defs \theta ConPurse[
    //              balance := ANY,
    //              exLog := ANY,
    //              name := ANY,
    //              nextSeqNo := 1 + old.conAuthPurse[name].nextSeqNo,
    //              pdAuth := \theta PayDetails[
    //                  from := name,
    //                  fromSeqNo := old.conAuthPurse[name].nextSeqNo,
    //                  to := cpd.name,
    //                  toSeqNo := cpd.nextSeqNo,
    //                  value := cpd.value],
    //              statuS := epr] \notin \ran~conAuthPurse
    // =
    // \forall other \in \ran~conAuthPurse &
    internal fun freshStartFromConPurse(purseName: Name, m: StartFrom): Boolean {
        val pd = mk_PayDetails(
            from = purseName,
            to = m.cpd.name,
            `value` = m.cpd.value,
            fromSeqNo = old.conAuthPurse[purseName].nextSeqNo,
            toSeqNo = m.cpd.nextSeqNo
        )
        // Ignoring the * mk_(purseName, \theta ConPurse) here
        return old.conAuthPurse.rng.filter { cp ->
            cp.status == Status.epr &&
                cp.nextSeqNo == 1UL + old.conAuthPurse[cp.name].nextSeqNo &&
                // check only for initialised pdAuth?  (cp.pdAuth != null implies ....) ?
                cp.properties.pdAuth == pd

        }.isEmpty()
    }

    // \forall pd: PayDetails | pd.from = name? @ pd.fromSeqNo < nextSeqNo
    internal fun payDetailsFromWithinNextSeqNo(name: Name, purseName: Name): Boolean {
        return forall(old.conAuthPurse.rng) { cp ->
            // Only worry after purse pdAuth bootstrapping has finished
            (cp.pdAuth != null && cp.properties.pdAuth.from == name) implies { cp.properties.pdAuth.fromSeqNo < old.conAuthPurse[purseName].nextSeqNo }
        }
    }

    // ZEVES-PRG126 Schema 8.20
    internal fun freshStartToConPurse(purseName: Name, m: StartTo): Boolean {
        val pd = mk_PayDetails(
            from = m.cpd.name,
            to = purseName,
            `value` = m.cpd.value,
            fromSeqNo = m.cpd.nextSeqNo,
            toSeqNo = old.conAuthPurse[purseName].nextSeqNo,
        )
        // Proof engineering only? Equivalent to mk_Req(pd) in old.ether?
//        val b1 = (mk_Req(pd) !in old.ether) implies {
//            purseName in old.conAuthPurse.dom &&
//                old.conAuthPurse[purseName].nextSeqNo < old.conAuthPurse[purseName].nextSeqNo
//        }
        val b1_2 = (mk_Req(pd) !in old.ether) implies { true }
        // B10 is not possible to declare here
        val b10 = true
        return b1_2 && b10 && old.conAuthPurse.rng.filter { cp ->
            cp.nextSeqNo == 1UL + old.conAuthPurse[cp.name].nextSeqNo &&
                cp.properties.pdAuth == pd &&
                cp.status == Status.epv
        }.isEmpty()
    }

    // \forall pd: PayDetails | pd.to = name? @ pd.toSeqNo < nextSeqNo
    internal fun payDetailsToWithinNextSeqNo(name: Name, purseName: Name): Boolean {
        return forall(old.conAuthPurse.rng) { cp ->
            // Only worry after purse pdAuth bootstrapping has finished
            (cp.pdAuth != null && cp.properties.pdAuth.from == name) implies { cp.properties.pdAuth.toSeqNo < old.conAuthPurse[purseName].nextSeqNo }
        }
    }

}
