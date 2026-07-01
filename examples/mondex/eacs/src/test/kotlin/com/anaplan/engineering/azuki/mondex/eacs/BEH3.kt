package com.anaplan.engineering.azuki.mondex.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexBehaviours
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexFunctions
import com.anaplan.engineering.azuki.mondex.dsl.MondexScenario
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2

@BEH(MondexBehaviours.IgnoreBehaviour, MondexFunctions.Ignore, """
    Nothing happens securely between purses
""")
class BEH3: MondexScenario() {

//    @Eac("Transfers can securely do nothing")
//    fun ignoreTransferDoesNothing() {
//        given {
//            thereIsAPurse(person1, 3, 1)
//            thereIsAPurse(person2, 2, 1)
//        }
//        whenever {
//            thereIsNoTransfer()
//        }
//        then {
//            worldExists {
//                personWithPurse(person1, 3, 1)
//                personWithPurse(person2, 2, 1)
//            }
//        }
//    }
//
//    @Eac("A transfer doing nothing will never create value in the world", """
//        The sum of all purses' balances does not increase.
//    """)
//    fun transferFailedImpliesNoValueCreation() {
//        given {
//            thereIsAPurse(person1, 3, 1)
//            thereIsAPurse(person2, 2, 1)
//        }
//        whenever {
//            thereIsNoTransfer()
//        }
//        then {
//            //noValueCreation()
//        }
//    }
//
//    @Eac("A transfer that does nothing will keep all value accounted for in the world", """
//        The sum of all purses' balances and lost components does not change.
//    """)
//    fun transferFailedImpliesAllValueAccounted() {
//        given {
//            thereIsAPurse(person1, 3, 1)
//            thereIsAPurse(person2, 2, 1)
//        }
//        whenever {
//            thereIsNoTransfer()
//        }
//        then {
//            //allValueAccounted()
//        }
//    }

}
