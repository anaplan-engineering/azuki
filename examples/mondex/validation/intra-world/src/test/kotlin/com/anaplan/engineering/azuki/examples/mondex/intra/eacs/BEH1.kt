package com.anaplan.engineering.azuki.examples.mondex.intra.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldFunctionalElements
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.IntraWorldScenario
import com.anaplan.engineering.azuki.mondex.FAILURE
import com.anaplan.engineering.azuki.mondex.SUCCESS
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2
import com.anaplan.engineering.azuki.mondex.world1

@BEH(1/*IntraWorldBehaviourConst.CreateWorld*/, IntraWorldFunctionalElements.World, """
    Create a world with authentic purses
""")
class BEH1 : IntraWorldScenario() {

    @Eac("Creates purses no transfer considering an explicit world available")
    fun explicitWorld() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 3)
            }
        }
        then {
            world1 hasTotalBalance 6
            person1 hasBalance 3
            person2 hasBalance 3
            person1 hasLost 0
            person2 hasLost 0
        }
    }
}
