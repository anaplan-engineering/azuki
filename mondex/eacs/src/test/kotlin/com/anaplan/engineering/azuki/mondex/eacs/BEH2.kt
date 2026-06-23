package com.anaplan.engineering.azuki.mondex.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexBehaviours
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexFunctions
import com.anaplan.engineering.azuki.mondex.dsl.MondexScenario
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2
import com.anaplan.engineering.azuki.mondex.person3

@BEH(MondexBehaviours.TransferBehaviour, MondexFunctions.Transfer, """
    Transfer money between purses
""")
class BEH2 : MondexScenario() {

    @Eac("Value can be transferred from one purse to another with a successful transfer", """
        Value moves from the sending purse's balance to the receiving purse's balance.
    """)
    fun successfulTransfer() {
        given {
            thereIsAPurse(person1, 3, 0)
            thereIsAPurse(person2, 2, 0)
        }
        whenever {
            thereIsATransfer(person1, person2, 2)
        }
        then {
            purseOf(person1) {
                hasBalance(1)
                hasLost(0)
            }
            purseOf(person2) {
                hasBalance(4)
                hasLost(0)
            }
        }
    }

    @Eac("Value can be lost with a failed transfer", """
        Value moves from the sending purse's balance to the sending purse's lost component.
        The receiving purse is unchanged.
    """)
    fun failedTransfer() {
        given {
            thereIsAPurse(person1, 3, 0)
            thereIsAPurse(person2, 2, 0)
        }
        whenever {
            thereIsATransfer(person1, person2, 2, false)
        }
        then {
            purseOf(person1) {
                hasBalance(1)
                hasLost(2)
            }
            purseOf(person2) {
                hasBalance(2)
                hasLost(0)
            }
        }
    }

    @Eac("Transfers do not affect the rest of the world")
    fun worldUnaffectedByTransfer() {
        given {
            thereIsAPurse(person1, 3, 0)
            thereIsAPurse(person2, 2, 0)
            thereIsAPurse(person3, 4, 1)
        }
        whenever {
            thereIsATransfer(person1, person2, 2)
        }
        then {
            worldExists {
                personWithPurse(person1, 1, 0)
                personWithPurse(person2, 4, 0)
                personWithPurse(person3, 4, 1)
            }
        }
    }

    @Eac("Transfers can securely do nothing")
    fun ignoreTransferDoesNothing() {
        given {
            thereIsAPurse(person1, 3, 0)
            thereIsAPurse(person2, 2, 0)
        }
        whenever {
            thereIsNoTransfer()
        }
        then {
            worldExists {
                personWithPurse(person1, 3, 0)
                personWithPurse(person2, 2, 0)
            }
        }
    }

//    @Eac("No value may be created in the system", """
//        The sum of the all the purses' balances does not increase.
//    """)
//    fun noValueCreation() {
//        given {
//            thereIsAPurse(person1, 3, 0)
//            thereIsAPurse(person2, 2, 0)
//            thereIsAPurse(person3, 4, 0)
//            // totalAbBalance(purses = (purse1, purse2, purse3), totalBalance = 9)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2)
//        }
//        then {
//            //totalAbBalance(purses = (purse1, purse2, purse3), totalBalance = 6)
//        }
//    }
}
