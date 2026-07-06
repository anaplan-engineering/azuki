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
 * Phase 7 — Inter-world refinement (Rab / Rbc).
 *
 * Placeholder EACs until [validation/inter-world] and inter-dsl adapters exist.
 * Intended to drive [RetrieveSystem] and PRG-126 §10.1 retrieve relations.
 */
@BEH(8, IntraWorldFunctionalElements.World, """
    Concrete protocol execution refines the abstract security model
""")
class BEH8 : IntraWorldScenario() {

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("A completed concrete transfer retrieves to the same abstract balances", """
        Refinement: Rab state retrieve after successful protocol (PRG-126 §10.1).
    """)
    fun concreteTransferImpliesAbstractTransfer() {
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
            // TODO: inter-world DSL — abstractView(world1) matches concrete balances via retrieve
            // TODO: worldExists(ABSTRACT) { person1 hasBalance 1; person2 hasBalance 4 }
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("No value is created at the between world during a successful transfer", """
        Security property lift: NoValueCreation at BetweenWorld (PRG-126 Ch.2 / Ch.5).
    """)
    fun betweenWorldPreservesNoValueCreation() {
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
                // TODO: worldExists(BETWEEN) { noValueCreation() }
                world1 hasTotalBalance 5
            }
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Abstract security properties hold of a concrete protocol run via retrieve", """
        Refinement check sketched in BEH1 commented code — noValueCreation(BETWEEN) lifted to ABSTRACT.
    """)
    fun abstractSecurityFromConcreteRun() {
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
            // TODO: worldExists(ABSTRACT) { allValueAccounted(from = BETWEEN) }
            // TODO: worldExists(BETWEEN) { allValueAccounted() }
            world1 hasTotalBalance 5
        }
    }
}
