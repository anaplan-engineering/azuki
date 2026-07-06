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
import com.anaplan.engineering.azuki.mondex.world1

/**
 * Phase 1 — Protocol step isolation.
 * Phase 2 — Complete happy path (incremental completion beyond [BEH2]).
 *
 * Step EACs drive [ConSystem.startTransfer], [BetweenWorld.reqOp], [BetweenWorld.valOp],
 * and [BetweenWorld.ackOp] without requiring the full end-to-end scenario first.
 */
@BEH(4, IntraWorldFunctionalElements.Transfer, """
    Execute individual protocol steps of a value transfer
""")
class BEH9 : IntraWorldScenario() {

    // --- Phase 1: request ---

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Starting a transfer does not yet move spendable balance")
    fun requestLeavesBalancesUnchanged() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            requestTransfer(transfer1)
        }
        then {
            person1 hasBalance 3
            person2 hasBalance 2
            person1 hasLost 0
            person2 hasLost 0
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("After starting a transfer, the payer enters epr and the payee enters epv", """
        Concrete purse status after StartFrom / StartTo (PRG-126 §4.9).
    """)
    fun requestAdvancesPurseStatus() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            requestTransfer(transfer1)
        }
        then {
            // TODO: DSL — person1 hasStatus epr
            // TODO: DSL — person2 hasStatus epv
            person1 hasBalance 3
            person2 hasBalance 2
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("After starting a transfer, protocol messages appear on the ether")
    fun requestPlacesMessagesOnEther() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            requestTransfer(transfer1)
        }
        then {
            // TODO: DSL — ether contains StartFrom / StartTo (or Req precursor) for transfer1
            person1 hasBalance 3
            person2 hasBalance 2
        }
    }

    // --- Phase 1: send (Req) ---

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Sending a transfer debits the payer but not the payee")
    fun sendDebitsPayerNotPayee() {
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

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("After send, total value is still accounted but the transfer is not complete")
    fun sendDoesNotCompleteTransfer() {
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
                // missing acknowledgement
            }
            then {
                // TODO: world1 allValueAccounted() at between level
                world1 hasTotalBalance 5
                person1 hasBalance 3
                person2 hasBalance 2
            }
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Sending a transfer increases sequence numbers on involved purses")
    fun sendIncrementsSequenceNumbers() {
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
            // TODO: DSL — person1 nextSeqNo > 0; person2 nextSeqNo > 0
            person1 hasBalance 1
            person2 hasBalance 2
        }
    }

    // --- Phase 1: acknowledge (Val + Ack) ---

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Acknowledging after send completes the transfer with correct final balances")
    fun ackCompletesSuccessfulTransfer() {
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
            acknowledgeTransfer(transfer1)
        }
        then {
            person1 hasBalance 1
            person2 hasBalance 4
            person1 hasLost 0
            person2 hasLost 0
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("After acknowledgement, both purses return to an idle-ready protocol state")
    fun ackReturnsPursesToEaTo() {
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
            acknowledgeTransfer(transfer1)
        }
        then {
            // TODO: DSL — person1 hasStatus eaTo; person2 hasStatus eaTo
            person1 hasBalance 1
            person2 hasBalance 4
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("After acknowledgement, authorised payment details reflect the completed transfer")
    fun ackClearsAuthorisedPaymentDetails() {
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
            acknowledgeTransfer(transfer1)
        }
        then {
            // TODO: DSL — person1 pdAuth matches transfer1 details (concrete)
            person1 hasBalance 1
            person2 hasBalance 4
        }
    }

    // --- Phase 2: security property completion (stub from BEH2) ---

    @Eac("A successful transfer will never create value in the world", """
        The sum of all purses' balances does not increase. Completes BEH2.transferImpliesNoValueCreation.
    """)
    fun transferImpliesNoValueCreation() {
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
                acknowledgeTransfer(transfer1)
            }
            then {
                // TODO: world1 noValueCreated()
                world1 hasTotalBalance 5
            }
        }
    }
}
