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
 * Phase 4 — Preconditions and security properties from PRG-126 Chapter 2.
 *
 * Drives [SecurityProperties.authentic], [SecurityProperties.sufficientFunds], and related preconditions
 * on [AbWorldFunctions.abTransferOkayTD] and concrete promotion pres.
 */
@BEH(5, IntraWorldFunctionalElements.Purse, """
    Transfers require authentic purses and sufficient funds
""")
class BEH5 : IntraWorldScenario() {

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("A transfer cannot succeed without sufficient funds in the from-purse", """
        Security property: SufficientFundsProperty (PRG-126 Ch.2).
    """)
    fun transferRequiresSufficientFunds() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 1)
                thereIsAPurse(person2, 0)
            }
        }
        then {
            world1 hasTotalBalance 1
            person1 hasBalance 1
            person2 hasBalance 0
        }
        successor {
            whenever {
                createTransfer(world1, transfer1, person1, person2, 5)
                requestTransfer(transfer1)
                sendTransfer(transfer1)
                acknowledgeTransfer(transfer1)
            }
            then {
                person1 hasBalance 1
                person2 hasBalance 0
                person1 hasLost 0
                person2 hasLost 0
            }
        }
    }

    @Eac("A transfer cannot involve a purse that is not in the authenticated world", """
        Security property: Authentic (PRG-126 Ch.2).
    """)
    fun transferRequiresAuthenticPurses() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                // TODO: attempt transfer to/from person2 not in world — needs DSL for unknown purse
            }
        }
        whenever {
            // TODO: createTransfer(world1, transfer1, person1, "unknown", 1)
        }
        then {
            // TODO: world unchanged; transfer must not succeed
            person1 hasBalance 3
        }
    }

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("A purse cannot transfer value to itself")
    fun selfTransferIsRejected() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
            }
        }
        then {
            person1 hasBalance 3
        }
        successor {
            whenever {
                createTransfer(world1, transfer1, person1, person1, 1)
                requestTransfer(transfer1)
                sendTransfer(transfer1)
                acknowledgeTransfer(transfer1)
            }
            then {
                person1 hasBalance 3
                person1 hasLost 0
            }
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails establish pdAuth for world"))
    @Eac("A zero-value transfer is handled consistently", """
        Stakeholder decision EAC — clarify whether 0-value transfer succeeds or is ignored.
    """)
    fun zeroAmountTransfer() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 0)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            acknowledgeTransfer(transfer1)
        }
        then {
            person1 hasBalance 3
            person2 hasBalance 2
        }
    }
}
