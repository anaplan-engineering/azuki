package com.anaplan.engineering.azuki.mondex.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexBehaviours
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexFunctionalElements
import com.anaplan.engineering.azuki.mondex.dsl.MondexScenario
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2

@BEH(MondexBehaviours.CreateWorld, MondexFunctionalElements.World, """
    Create a world with authentic purses
""")
class BEH1 : MondexScenario() {

    @Eac("Transfer between authentic purses succeeds")
    fun transferOkay() {
        given {
            thereIsAWorld {
                personWithPurse(person1, 3, 0)
                personWithPurse(person2, 2, 1)
            }
        }
        whenever {
            thereIsATransfer(person1, person2, 3)
        }
        then {
            purseExists(person1, 0, 0)
            purseExists(person2, 5, 1)
            purseOf(person1) {
                hasBalance(0)
                hasLost(0)
            }
            purseOf(person2) {
                hasBalance(5)
                hasLost(1)
                hasLost(1)
            }
            worldExists {
                personWithPurse(person1, 0, 0)
                personWithPurse(person2, 5, 1)
            }
        }
    }
}
