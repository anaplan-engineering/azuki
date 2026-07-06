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
import com.anaplan.engineering.azuki.mondex.transfer1
import com.anaplan.engineering.azuki.mondex.world1

@BEH(
    3, IntraWorldFunctionalElements.Transfer, """
    Transfer money securely between purses through non-atomic steps. Exercise both success, abort and ignore paths.

    PRG126 3.4: AbInitState
     One conventional definition of the initial state of a system is as being empty; operations are used to add elements
     to the state until the desired configuration is reached.

    PGG126 6.1: BetweenInitState
    As with the abstract case, we set up a particular initial between state.
    We do not want to model adding new authentic purses to the system, since some of the operations involved are
    outside the security boundary.  So we allow the world to be `switched off' and a new world `switched on',
    where the new world consists of the old world as it was, plus the new purses. So our initial state must allow
    purses to be part-way through transactions.
""")
class BEH4 : IntraWorldScenario() {

    @Eac("Cannot create new purses in a world that has already been created")
    fun cannotAddPursesToCreatedWorld() {
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
        successor {
            whenever {
                addANewPurse(world1, person3)
            }
        }
    }
}
