package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.as_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.is_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack_Module.mk_Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.AuxWorld_Module.mk_AuxWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.AuxWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Clear_Module.mk_Clear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear_Module.as_ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear_Module.is_ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear_Module.mk_ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.as_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.is_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult_Module.mk_ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PDProtectedMessage_Module.as_PDProtectedMessage
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
import com.anaplan.engineering.azuki.examples.mondex.specification.mondexPretty
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
        // \forall pd: PayDetails & (req(pd) \in ether \land ack(pd) !in ether)\iff (pd \in toInEpv \union toLogged)
        // = [iff-def]
        // \forall pd: PayDetails & ((req(pd) \in ether \land ack(pd) !in ether) \implies (pd \in toInEpv \union toLogged)) \land
        //                          ((pd \in toInEpv \union toLogged) \inmplies (req(pd) \in ether \land ack(pd) !in ether))
        // = [forall-dist-and]
        // (\forall pd: PayDetails & (req(pd) \in ether \land ack(pd) !in ether) \implies (pd \in toInEpv \union toLogged)) \land
        // (\forall pd: PayDetails & (pd \in toInEpv \union toLogged) \implies (req(pd) \in ether \land ack(pd) !in ether))
        // = [ball-implies]
        // (req(pd) \in ether \land ack(pd) !in ether \implies pd \in toInEpv \union toLogged) \land
                // A and !B implies C
                // !(A and !B) || C
                // !A || B || C
                // !is_Req(m) || is_Ack(m) || m.pd in toInEpv + toLogged
        // (pd \in toInEpv \union toLogged \implies req(pd) \in ether \land ack(pd) !in ether)
//        forall(ether) { m ->
//            // need to choose between all options, not "fix" on a req option, given the "req" message won't be in EPV! (or not be necessarily logged)
//            !is_Req(m) || is_Ack(m) || as_PDProtectedMessage(m).pd in properties.toInEpv + properties.toLogged
//            //as_Req(m).pd in (properties.toInEpv + properties.toLogged)
//        } &&
        forall(ether) { m ->
            val r = (is_Req(m) && !is_Ack(m)) implies { as_Req(m).pd in properties.toInEpv + properties.toLogged }
            if (!r) { println("FAILED B10 for m=${m.mondexPretty()}"); return@forall true }
            r
        } &&
        forall(properties.toInEpv + properties.toLogged) { pd -> mk_Req(pd) in ether && mk_Ack(pd) !in ether }

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
            try {
                mk_(old.transform(
                    conAuthPurse = old.conAuthPurse * mk_(name, cdash),
                    ether = old.ether + mk_Set(mbang),
                ),
                    mbang)
            } catch (p: PreconditionFailure) {
                println("FAILED!\n\t"+mk_AuxWorld(old.conAuthPurse * mk_(name, cdash), old.ether + mk_Set(mbang), old.archive).mondexPretty())
                throw p
            }
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
            val purse = old.conAuthPurse[name]
            val ignorePre = old.functions.ignore.pre(name, m)
            val abortPre = old.functions.abortOnly.pre(name, m)
            val startFromOkayPre = old.functions.phiBOp.pre(name, m, purse.functions.startFromPurseOkay)
                //purse.functions.startFromPurseEaFromOkay)
            //@4paper - proof engineering checks don't need to feature; repeated checks aren't expensive to run
            val startFromPre =
                purse.status == Status.eaFrom &&
                    purse.nextSeqNo < MAX_NAT &&
                    is_StartFrom(m) &&
                    Bottom in old.ether &&
                    payDetailsFromWithinNextSeqNo(name, purse.name) &&
                    // Removed, given argument against Lemma 8.26/27 here
                    //((purseName in old.conAuthPurse.dom) implies { purseName != name }) &&
                    freshStartFromConPurse(purse.name, as_StartFrom(m))
            setOf(ignorePre, abortPre, startFromOkayPre, startFromPre).all { it }
        },
        //@4paper - manual proofs always took the abort path; ZEVES-PRG126 Ch8 shows the complexity of the consequences
//        post = { name, m, result ->
//            val (choice, r) = result
//            val (dash, mbang) = r
//            when (choice) {
//                UnprotectedPromotionChoice.Ignore -> old.functions.ignore.post(name, m, r)
//                UnprotectedPromotionChoice.Abort  -> old.functions.abortOnly.post(name, m, r)
//                UnprotectedPromotionChoice.Act    -> old.functions.phiBOp.post(name, m, purse.functions.startFromPurseOkay, r)
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
            val purse = old.conAuthPurse[name]
            val ignorePre = old.functions.ignore.pre(name, m)
            val abortPre = old.functions.abortOnly.pre(name, m)
            val startToOkayPre = old.functions.phiBOp.pre(name, m, purse.functions.startToPurseOkay)
            val startToPre =
                is_StartTo(m) &&
                    purse.status == Status.eaFrom &&
                    purse.nextSeqNo < MAX_NAT &&
                    as_StartTo(m).cpd.nextSeqNo < MAX_NAT &&
                    payDetailsToWithinNextSeqNo(name, purse.name) &&
                    //((purseName in old.conAuthPurse.dom) implies { purseName != name }) &&
                    freshStartToConPurse(purse.name, as_StartTo(m))
            setOf(ignorePre, abortPre, startToOkayPre, startToPre).all { it }
        },
    )

    // req already used for the message so extended names to include op

    val reqOp = function(
        //TODO use an extra parameter for this command?
        command = { name: Name, m: Message ->
            val choice = old.choice.nextProtected()
            val r = when (choice) {
                ProtectedPromotionChoice.Ignore -> old.functions.ignore(name, m)
                ProtectedPromotionChoice.Act    -> old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.reqPurseOkay)
            }
            mk_(choice, r)
        },
        pre = { name, m ->
            val purse = old.conAuthPurse[name]
            val pdAuth = purse.pdAuth // avoid .properties.pdAuth to avoid PreConditionFailure within a pre call
            val ignorePre = old.functions.ignore.pre(name, m)
            val reqOkayPre = old.functions.phiBOp.pre(name, m, purse.functions.reqPurseOkay)
            // ZEVES-PRG126 Table 8.3, p.87, Schema 8.21, Theorem 8.37
            //TODO LF - how much of this is just ZEVES proof engineering needs?
            val reqPurseOkayPre =
                is_Req(m) &&
                purse.status == Status.epr &&
                purse.functions.reqPurseOkay.pre(m) &&
                (pdAuth != null && mk_Val(pdAuth) !in old.ether) implies {
                    pdAuth != null &&
                    // B3 - no future val msgs on given pdAuth
                    pdAuth.to in old.conAuthPurse.dom &&
                    pdAuth.from in old.conAuthPurse.dom &&
                    (pdAuth.to != name) implies { pdAuth.toSeqNo < old.conAuthPurse[pdAuth.to].nextSeqNo } &&
                    (pdAuth.from != name) implies { pdAuth.fromSeqNo < old.conAuthPurse[pdAuth.from].nextSeqNo } //&&
                    // B9 - epr disjoint from val / ack in ether TODO LF - equivalent; prefers the first?
//                    pdAuth !in old.properties.fromInEpr
//                    old.properties.fromInEpr.all { pdIn -> pdIn != pdAuth } &&
                    // B11 - val pdAuth is either archived or exception logged
//                    (mk_(pdAuth.from, pdAuth) !in old.archive) implies {
//                        pdAuth in old.conAuthPurse[pdAuth.from].exLog }
                }
            setOf(ignorePre, reqOkayPre, reqPurseOkayPre).all { it }
        },
//        post = { name, m, result ->
//            val (choice, r) = result
//            val (dash, mbang) = r
//            old.conAuthPurse[name].functions.reqPurseOkay.post(
//                m, mk_(dash.conAuthPurse[name], mbang))
//        }
    )

    val valOp = function(
        command = { name: Name, m: Message ->
            val choice = old.choice.nextProtected()
            val r = when (choice) {
                ProtectedPromotionChoice.Ignore -> old.functions.ignore(name, m)
                ProtectedPromotionChoice.Act    -> old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.valPurseOkay)
            }
            mk_(choice, r)
        },
        pre = { name, m ->
            val purse = old.conAuthPurse[name]
            val pdAuth = purse.pdAuth
            val ignorePre = old.functions.ignore.pre(name, m)
            val valOkayPre = old.functions.phiBOp.pre(name, m, purse.functions.valPurseOkay)
            // ZEVES-PRG126 Table 8.3, p.87, Schema 8.22, Theorem 8.38
            //TODO LF - how much of this is just ZEVES proof engineering needs?
            val valPurseOkayPre =
                is_Val(m) &&
                purse.status == Status.epv &&
                purse.functions.valPurseOkay.pre(m) &&
                // B4 - no future ack messages in ether
                (pdAuth != null && mk_Ack(pdAuth) !in old.ether) implies { pdAuth?.to in old.conAuthPurse.dom }
            setOf(ignorePre, valOkayPre, valPurseOkayPre).all { it }
        },
//        post = { name, m, result ->
//            val (dash, mbang) = result
//            // needs more when we have disjunction
//            val valOkayPost = old.conAuthPurse[name].functions.valPurseOkay.post(
//                m, mk_(dash.conAuthPurse[name], mbang)
//            )
//
//            valOkayPost
//        }
    )

    val ackOp = function(
        command = { name: Name, m: Message ->
            val choice = old.choice.nextProtected()
            val r = when (choice) {
                ProtectedPromotionChoice.Ignore -> old.functions.ignore(name, m)
                ProtectedPromotionChoice.Act    -> old.functions.phiBOp(name, m, old.conAuthPurse[name].functions.ackPurseOkay)
            }
            mk_(choice, r)
        },
        pre = { name, m ->
            val purse = old.conAuthPurse[name]
            val pdAuth = purse.properties.pdAuth
            val ignorePre = old.functions.ignore.pre(name, m)
            val ackOkayPre = old.functions.phiBOp.pre(name, m, purse.functions.ackPurseOkay)
            // ZEVES-PRG126 Table 8.3, p.87, Schema 8.23, Theorem 8.38
            //TODO LF - how much of this is just ZEVES proof engineering needs?
            val ackPurseOkayPre =
                is_Ack(m) &&
                    purse.status == Status.epa &&
                    purse.functions.ackPurseOkay.pre(m)
            setOf(ignorePre, ackOkayPre, ackPurseOkayPre).all { it }
        },
//        post = { name, m, result ->
//            val (dash, mbang) = result
//            val ackOkayPost = old.conAuthPurse[name].functions.ackPurseOkay.post(
//                m, mk_(dash.conAuthPurse[name], mbang)
//            )
//
//            ackOkayPost
//        }
    )

    /**
     * There is a four stage protocol for reading and clearing exception
     * logs: reading a log to the ether, copying a log from the ether to the
     * archive, authorising a purse exception log clear based on what's in
     * the archive, and clearing a purse's exception log having received
     * authorisation.
     *
     * We note that as a result of this protocol, if {\it
     * Clear\-Exception\-Log\-Purse\-Okay} aborts and logs an uncompleted
     * transaction, then the purse's exception log will not be cleared.  The
     * reason for this is as follows.  The purse gets to $eaFrom$ by
     * aborting any uncompleted transaction.  If this would create a new
     * exception record, the clear transaction could not occur, because the
     * (imaged) exception log in the message would not match the actual
     * exception log in the purse.
     */
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
//                allPayDetails().all { pd -> pd !in old.conAuthPurse[name].exLog } &&
//                purse.transform(exLog = emptySet()) !in old.conAuthPurse.rng
                TODO("Choice between ignore and clearExceptionLogPurseOkay")
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

    /**
     * The message to clear an exception log can be created only for log
     * details which are already recorded in the archive.  The clear code of
     * the message is based on the selected logs in the archive.  The
     * exception log clear message couples this clear code with the name of a
     * purse.  This supports constraint B--\ref{b-req-clear} which requires
     * that this operation not put a clear message into the ether if the
     * relevant logs have not been archived.
     *
     * Exception logs must be kept for all time to ensure that all value
     * remains accounted for.  The operation to clear purses of their
     * exception logs must be supported by a mechanism to store the cleared
     * logs.  This is what the archive supplies.
     *
     * The purse supports the $ReadExceptionLog$ operation, which puts an
     * exception log record into the $ether$ as a message.  As the system
     * implementers have no control over the $ether$, we have modelled it as
     * lossy at the concrete level, allowing for messages to be lost from the
     * $ether$ at any time.
     *
     * The $archive$ is a {\sl secure} store for information, and to support
     * the security of the purse there must be a manual mechanism to move log
     * messages from the $ether$ into the $archive$ for safe keeping.  This
     * is modelled by the $Archive$ operation, and is implemented by some
     * mechanism external to the target of evaluation.
     */
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

    /**
     * There are some operations on the world that do not have equivalents on
     * individual purses.  These are not implemented by the target of
     * evaluation, but need to be implemented by some manual means or
     * external system.
     *
     * The $archive$ is a {\sl secure} store for information, and to support
     * the security of the purse there must be a manual mechanism to move log
     * messages from the $ether$ into the $archive$ for safe keeping.  This
     * is modelled by the $Archive$ operation, and is implemented by some
     * mechanism external to the target of evaluation.
     */
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
