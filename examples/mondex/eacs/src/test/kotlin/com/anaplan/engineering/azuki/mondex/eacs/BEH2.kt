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
    Transfer money securely between purses
""")
class BEH2 : MondexScenario() {

//    @Eac("Value can be transferred from one purse to another with a successful transfer", """
//        Value moves from the sending purse's balance to the receiving purse's balance. The lost
//        components do not change.
//    """)
//    fun successfulTransfer() {
//        given {
//            thereIsAPurse(person1, 3, 0)
//            thereIsAPurse(person2, 2, 0)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2, true)
//        }
//        then {
//            purseOf(person1) {
//                hasBalance(1)
//                hasLost(0)
//            }
//            purseOf(person2) {
//                hasBalance(4)
//                hasLost(0)
//            }
//        }
//    }
//
//    @Eac("Value can be lost with a failed transfer", """
//        Value moves from the sending purse's balance to the sending purse's lost component.
//        The receiving purse is unchanged.
//    """)
//    fun failedTransfer() {
//        given {
//            thereIsAPurse(person1, 3, 1)
//            thereIsAPurse(person2, 2, 0)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2, false)
//        }
//        then {
//            purseOf(person1) {
//                hasBalance(1)
//                hasLost(3)
//            }
//            purseOf(person2) {
//                hasBalance(2)
//                hasLost(0)
//            }
//        }
//    }
//
//    @Eac("Transfers do not affect the rest of the world")
//    fun worldUnaffectedByTransfer() {
//        given {
//            thereIsAPurse(person1, 3, 0)
//            thereIsAPurse(person2, 2, 0)
//            thereIsAPurse(person3, 4, 1)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2)
//        }
//        then {
//            worldExists {
//                personWithPurse(person1, 1, 0)
//                personWithPurse(person2, 4, 0)
//                personWithPurse(person3, 4, 1)
//            }
//        }
//    }
//
//    @Eac("A successful transfer will never create value in the world", """
//        The sum of all purses' balances does not increase.
//    """)
//    fun transferImpliesNoValueCreation() {
//        given {
//            thereIsAPurse(person1, 3, 0)
//            thereIsAPurse(person2, 2, 0)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2, true)
//        }
//        then {
//            //noValueCreation()
//        }
//    }
//
//    @Eac("A successful transfer will keep all value accounted for in the world", """
//        The sum of all purses' balances and lost components does not change.
//    """)
//    fun transferImpliesAllValueAccounted() {
//        given {
//            thereIsAPurse(person1, 3, 0)
//            thereIsAPurse(person2, 2, 0)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2, true)
//        }
//        then {
//            //allValueAccounted()
//        }
//    }
//
//    @Eac("A failed transfer will never create value in the world", """
//        The sum of all purses' balances does not increase.
//    """)
//    fun transferFailedImpliesNoValueCreation() {
//        given {
//            thereIsAPurse(person1, 3, 0)
//            thereIsAPurse(person2, 2, 0)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2, false)
//        }
//        then {
//            //noValueCreation()
//        }
//    }
//
//    @Eac("A failed transfer will keep all value accounted for in the world", """
//        The sum of all purses' balances and lost components does not change.
//    """)
//    fun transferFailedImpliesAllValueAccounted() {
//        given {
//            thereIsAPurse(person1, 3, 0)
//            thereIsAPurse(person2, 2, 0)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 2, false)
//        }
//        then {
//            //allValueAccounted()
//        }
//    }
}
