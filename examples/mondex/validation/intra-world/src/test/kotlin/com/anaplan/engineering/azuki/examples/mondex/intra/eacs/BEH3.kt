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
import com.anaplan.engineering.azuki.mondex.world1

@BEH(
    3, IntraWorldFunctionalElements.Transfer, """
    Transfer money securely between purses through non-atomic steps. Exercise both success, abort and ignore paths
""")
class BEH3 : IntraWorldScenario() {

    @KnownBug(Issue(ConcreteWorld, "fails pre start composition - needs splitting?"))
    @Eac("Value can be transferred from one purse to another with a successful transfer", """
        Value moves from the sending purse's balance to the receiving purse's balance. The lost
        components do not change.
    """)
    fun successfulTransfer() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            // Create transfer updates the ConPurse pdAuth: PayDetails
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

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("Value can be lost with a failed transfer", """
        Value moves from the sending purse's balance to the sending purse's lost component.
        The receiving purse is unchanged.
    """)
    fun failedTransfer() {
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
    @Eac("Transfers do not affect the rest of the world")
    fun worldUnaffectedByTransfer() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
                thereIsAPurse(person3, 12)
                thereIsAPurse(person4, 21)
            }
        }
        whenever {
            createTransfer(world1, transfer1, person1, person2, 2)
            requestTransfer(transfer1)
            sendTransfer(transfer1)
            acknowledgeTransfer(transfer1)
        }
        then {
            person3 hasBalance 12
            person4 hasBalance 21
        }
    }

    @Eac("A successful transfer will never create value in the world", """
        The sum of all purses' balances does not increase.
    """)
    fun transferImpliesNoValueCreation() {
        given {
            thereIsAPurse(person1, 3)
            thereIsAPurse(person2, 2)
        }
        whenever {
//            thereIsATransfer(person1, person2, 2, true)
        }
        then {
            //noValueCreation()
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("A successful transfer will keep all value accounted for in the world", """
        The sum of all purses' balances and lost components does not change.
    """)
    fun transferImpliesAllValueAccounted() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        then {
            world1 hasTotalBalance 5
            person1 hasBalance 3
            person2 hasBalance 2
        }
        successor {
            whenever {
                createTransfer(world1, transfer1, person1, person2, 2)
                requestTransfer(transfer1)
                sendTransfer(transfer1)
                acknowledgeTransfer(transfer1)
            }
            then {
                world1 hasTotalBalance 5
                person1 hasBalance 1
                person2 hasBalance 4
            }
        }
    }

    @KnownBug(Issue(AbstractWorld, "fails system post"), Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("A failed transfer will never create value in the world", """
        The sum of all purses' balances does not increase.
    """)
    fun transferFailedImpliesNoValueCreation() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        then {
            world1 hasTotalBalance 5
            person1 hasBalance 3
            person2 hasBalance 2
        }
        successor {
            whenever {
                createTransfer(world1, transfer1, person1, person2, 2)
                requestTransfer(transfer1)
                sendTransfer(transfer1)
                abortTransfer(transfer1)
            }
            then {
                world1 hasTotalBalance 5
                person1 hasBalance 1
                person2 hasBalance 2
                person1 hasLost 2
                person2 hasLost 0
            }
        }
    }

    @KnownBug(Issue(ConcreteWorld, "fails pre abort?"))
    @Eac("A failed transfer will never create value in the world", """
        The sum of all purses' balances does not increase.
    """)
    fun transferFailedImpliesNoValueCreation_noLoss() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        then {
            world1 hasTotalBalance 5
            person1 hasBalance 3
            person2 hasBalance 2
        }
        successor {
            whenever {
                createTransfer(world1, transfer1, person1, person2, 2)
                requestTransfer(transfer1)
                abortTransfer(transfer1)
            }
            then {
                world1 hasTotalBalance 5
                person1 hasBalance 3
                person2 hasBalance 2
                person1 hasLost 0
                person2 hasLost 0
            }
        }
    }

    // * ConPurse related sanity checks
    //      * startFromPurseOkay, startToPurseOkay, etc. (PRG sect 4.7.1)
    //      * preparing promotion to inject ConPurse in ConWorld, will neeed startFromEaFromPurseOkay (PRG sect 4.9.1)
    //      * note that startFromPurseOkay may abort, whereas startFromEaFromPurseOkay is success case
}
