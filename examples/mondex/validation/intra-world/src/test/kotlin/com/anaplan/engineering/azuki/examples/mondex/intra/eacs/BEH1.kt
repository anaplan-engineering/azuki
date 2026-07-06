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
            person1 hasBalance 3
            person2 hasBalance 3
            person1 hasLost 0
            person2 hasLost 0
        }
    }

    @Eac("Creates purses with successful transfer considering an explicit world available")
    fun explicitWorldAtomicTransferOkay() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 3)
            }
        }
        whenever {
            makeATransfer(person1, person2, 2, SUCCESS)
        }
        then {
            person1 hasBalance 1
            person2 hasBalance 5
            person1 hasLost 0
            person2 hasLost 0
        }
    }

    @Eac("A failed transfer will keep all value accounted for in the world", """
        The sum of all purses' balances and lost components does not change.
    """)
    fun transferFailedImpliesAllValueAccounted() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        whenever {
            makeATransfer(person1, person2, 2, FAILURE)
        }
        then {
            worldHasTotalBalance(world1, 5)
            person1 hasBalance 1
            person2 hasBalance 2
            person1 hasLost 2
            person2 hasLost 0
        }
    }

    //LF @EK might matter here to know which world you are operating under, so something like...
    //    here defaults to ABSTRACT on the inner calls.
    //    if the DSL is together, there is a lot care to ensure that the level of check is in the right place
    //    (e.g. worldExists(ABSTRACT) { noValueCreation(BETWEEN) } is a refinement check not a sanity one)

    //LF @EK you might want between world checks? doesn't make sense in the abstract. No-Op for abstract?
    //    there will be loads of such no ops for abstract as you go down the chain
    //
    // * ConPurse related sanity checks
    //      * startFromPurseOkay, startToPurseOkay, etc. (PRG sect 4.7.1)
    //      * preparing promotion to inject ConPurse in ConWorld, will neeed startFromEaFromPurseOkay (PRG sect 4.9.1)
    //      * note that startFromPurseOkay may abort, whereas startFromEaFromPurseOkay is success case
}
