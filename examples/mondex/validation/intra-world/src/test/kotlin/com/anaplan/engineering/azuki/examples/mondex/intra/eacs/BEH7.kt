package com.anaplan.engineering.azuki.examples.mondex.intra.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldFunctionalElements
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.IntraWorldScenario
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2
import com.anaplan.engineering.azuki.mondex.person3
import com.anaplan.engineering.azuki.mondex.person4
import com.anaplan.engineering.azuki.mondex.transfer1
import com.anaplan.engineering.azuki.mondex.transfer2
import com.anaplan.engineering.azuki.mondex.world1

/**
 * Phase 6 — Multiple transfers and interleaving.
 *
 * Drives purse return to idle state, sequence-number constraints in [BetweenWorld.startFrom],
 * and global fromInEpr constraints in [BetweenWorld.reqOp].
 */
@BEH(7, IntraWorldFunctionalElements.Transfer, """
    Execute multiple transfers in sequence or in a populated world
""")
class BEH7 : IntraWorldScenario() {

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Two successful transfers in sequence produce correct final balances")
    fun twoSuccessfulTransfersInSequence() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 10)
                thereIsAPurse(person2, 0)
            }
        }
        whenever {
            // Transfer 1: person1 → person2, amount 5
            createTransfer(world1, transfer1, person1, person2, 5)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            acknowledgeTransfer(transfer1)
            // Transfer 2: person2 → person1, amount 3
            createTransfer(world1, transfer2, person2, person1, 3)
            requestTransfer(transfer2)
            sendTransfer(transfer2)
            acknowledgeTransfer(transfer2)
        }
        then {
            person1 hasBalance 8   // 10 - 5 + 3
            person2 hasBalance 2   // 0 + 5 - 3
        }
    }

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("A successful transfer is possible after a prior failed transfer with lost value")
    fun secondTransferAfterFailedFirst() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 10)
                thereIsAPurse(person2, 0)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 5)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            abortTransfer(transfer1)
            createTransfer(world1, transfer2, person1, person2, 2)
            requestTransfer(transfer2)
            sendTransfer(transfer2)
            acknowledgeTransfer(transfer2)
        }
        then {
            person1 hasBalance 3   // 10 - 5 (lost) - 2 (sent) + 0 received on retry from p2... TODO verify amounts
            person1 hasLost 5
            person2 hasBalance 2
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Transfers between different purse pairs do not interfere with each other")
    fun concurrentTransfersDifferentPairs() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 10)
                thereIsAPurse(person2, 0)
                thereIsAPurse(person3, 20)
                thereIsAPurse(person4, 0)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 5)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            acknowledgeTransfer(transfer1)
            createTransfer(world1, transfer2, person3, person4, 7)
            requestTransfer(transfer2)
            sendTransfer(transfer2)
            acknowledgeTransfer(transfer2)
        }
        then {
            person1 hasBalance 5
            person2 hasBalance 5
            person3 hasBalance 13
            person4 hasBalance 7
        }
    }

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Aborting one transfer does not prevent completing another transfer in the same world")
    fun interleavedAbortAndSuccess() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 10)
                thereIsAPurse(person2, 0)
                thereIsAPurse(person3, 10)
                thereIsAPurse(person4, 0)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 5)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            abortTransfer(transfer1)
            createTransfer(world1, transfer2, person3, person4, 4)
            requestTransfer(transfer2)
            sendTransfer(transfer2)
            acknowledgeTransfer(transfer2)
        }
        then {
            person1 hasLost 5
            person3 hasBalance 6
            person4 hasBalance 4
        }
    }
}
