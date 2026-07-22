package com.anaplan.engineering.azuki.examples.mondex.intra.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldFunctionalElements
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.IntraWorldScenario
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2
import com.anaplan.engineering.azuki.mondex.transfer1
import com.anaplan.engineering.azuki.mondex.transfer1Ref
import com.anaplan.engineering.azuki.mondex.world1

/**
 * Phase 3 — Abort matrix at each protocol point.
 *
 * Covers abort-after-create, abort-after-request, abort-after-send (loss + logging).
 * Drives [BetweenWorld.abort], [AbSystem.lostTransfer], [AbSystem.ignore], and LogIfNecessary.
 */
@BEH(6, IntraWorldFunctionalElements.Transfer, """
    Abort a transfer safely at each point in the protocol
""")
class BEH6 : IntraWorldScenario() {

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Aborting immediately after creating a transfer record does not change balances")
    fun abortAfterCreateDoesNothing() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            abortTransfer(transfer1)
        }
        then {
            person1 hasBalance 3
            person2 hasBalance 2
            person1 hasLost 0
            person2 hasLost 0
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Aborting after request but before send does not create lost value")
    fun abortAfterRequestNoLoss() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            requestTransfer(transfer1)
            abortTransfer(transfer1)
        }
        then {
            person1 hasBalance 3
            person2 hasBalance 2
            person1 hasLost 0
            person2 hasLost 0
        }
    }

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Aborting after send moves value from payer balance to payer lost", """
        Extends BEH2.failedTransfer — abort matrix row: after send.
    """)
    fun abortAfterSendRecordsLoss() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            abortTransfer(transfer1)
        }
        then {
            person1 hasBalance 1
            person2 hasBalance 2
            person1 hasLost 2
            person2 hasLost 0
        }
    }

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Aborting after send records the transfer in the payer exception log", """
        Security property: LogIfNecessary (PRG-126 Ch.2).
    """)
    fun abortAfterSendLogsException() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            abortTransfer(transfer1)
        }
        then {
            person1 hasLost 2
            person1 exceptionLogContains transfer1Ref
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails message composition"))
    @Eac("A failed transfer will keep all value accounted for in the world", """
        Completes BEH2.transferFailedImpliesAllValueAccounted stub.
        The sum of all purses' balances and lost components does not change.
    """)
    fun transferFailedImpliesAllValueAccounted() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        then {
            world1 hasTotalBalance 5
        }
        successor {
            whenever {
                createTransfer(world1, transfer1, person1, person2, 2)
                requestTransfer(transfer1)
                sendTransfer(transfer1)
                abortTransfer(transfer1)
            }
            then {
                // TODO: world1 allValueAccounted()
                world1 hasTotalValue 5
                world1 hasTotalBalance 3
                world1 hasTotalLost 2
                person1 hasBalance 1
                person1 hasLost 2
                person2 hasBalance 2
            }
        }
    }
}
