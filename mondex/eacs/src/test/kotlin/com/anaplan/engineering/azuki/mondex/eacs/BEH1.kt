package com.anaplan.engineering.azuki.mondex.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexBehaviours
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexFunctionalElements
import com.anaplan.engineering.azuki.mondex.dsl.MondexScenario
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2
import com.anaplan.engineering.azuki.mondex.person3

@BEH(MondexBehaviours.CreateWorld, MondexFunctionalElements.World, """
    Create a world with authentic purses
""")
class BEH1 : MondexScenario() {

//    @Eac("old way")
//    fun test1() {
//        given {
//            thereIsAWorld {
//                personWithPurse(person1, 3, 0)
//                personWithPurse(person2, 2, 1)
//            }
//            thereIsAPurse(person3, 6, 1)
//        }
//        whenever {
//            thereIsATransfer(person1, person2, 3)
//        }
//        then {
//            purseExists(person1, 0, 0)
//            purseExists(person2, 5, 1)
//            purseOf(person1) {
//                hasBalance(0)
//                hasLost(0)
//            }
//            purseOf(person2) {
//                hasBalance(5)
//                hasLost(1)
//            }
//            worldExists {
//                personWithPurse(person1, 0, 0)
//                personWithPurse(person2, 5, 1)
//                personWithPurse(person3, 6, 1)
//            }
//        }
//    }

    @Eac("new way")
    fun test2() {
        given {
            thereIsAPurse(person1, 3, 0)
            thereIsAPurse(person2, 3, 2)
        }
        whenever {
            thereIsATransfer(person1, person2, 1)
        }
        then {
            purseExists(person1, 2, 0)
            purseOf(person2) {
                hasBalance(4)
                hasLost(2)
            }
            worldExists {
                personWithPurse(person1, 2, 0)
                personWithPurse(person2, 4, 2)
            }
        }
    }
}
