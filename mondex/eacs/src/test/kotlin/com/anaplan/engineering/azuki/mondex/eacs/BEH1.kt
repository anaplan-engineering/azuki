package com.anaplan.engineering.azuki.mondex.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexBehaviours
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexFunctionalElements
import com.anaplan.engineering.azuki.mondex.dsl.MondexScenario
import com.anaplan.engineering.azuki.mondex.person1

@BEH(MondexBehaviours.CreateWorld, MondexFunctionalElements.World, """
    Create a world with authentic purses
""")
class BEH1 : MondexScenario() {

    @Eac("When a purse is defined, it is part of the world")
    fun purseIsPartOfWorld() {
        given {
            thereIsAPurse(person1, 3, 0)
        }
        then {
            worldExists {
                personWithPurse(person1, 3, 0)
            }
        }
    }

}
