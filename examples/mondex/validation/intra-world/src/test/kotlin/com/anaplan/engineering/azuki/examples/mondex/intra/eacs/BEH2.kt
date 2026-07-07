package com.anaplan.engineering.azuki.examples.mondex.intra.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldFunctionalElements
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.IntraWorldScenario
import com.anaplan.engineering.azuki.mondex.FAILURE
import com.anaplan.engineering.azuki.mondex.SUCCESS
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2
import com.anaplan.engineering.azuki.mondex.person3
import com.anaplan.engineering.azuki.mondex.person4
import com.anaplan.engineering.azuki.mondex.transfer1
import com.anaplan.engineering.azuki.mondex.world1

@BEH(
    // TODO --forgot how limited annotations were :( may see if I can use reflection to generate constants!
    3, IntraWorldFunctionalElements.Transfer, """
    Atomic transfers money securely between purses. Exercise both success, abort paths.
""")
class BEH2 : IntraWorldScenario() {

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
            world1 hasTotalValue 6
            world1 hasTotalBalance 6
            world1 hasTotalLost 0
            person1 hasBalance 1
            person2 hasBalance 5
            person1 hasLost 0
            person2 hasLost 0
        }
    }

    @Eac("Creates purses with successful transfer considering an explicit world available", """
        This confirms the security property that no value is created and all amounts are accounted for
    """)
    fun explicitWorldAtomicTransferOkayCheckBeforeAfter() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 3)
            }
        }
        then {
            //LF these two first checks deserve a DSL call (e.g. SP1-noValueCreation and SP2-allAccountedFor)?
            world1 hasTotalValue 6
            world1 hasTotalBalance 6
            world1 hasTotalLost 0
            person1 hasBalance 3
            person2 hasBalance 3
            person1 hasLost 0
            person2 hasLost 0
        }
        successor {
            whenever {
                makeATransfer(person1, person2, 2, SUCCESS)
            }
            then {
                // Same check, but different balances per purse
                world1 hasTotalValue 6
                world1 hasTotalBalance 6
                world1 hasTotalLost 0
                person1 hasBalance 1
                person2 hasBalance 5
                person1 hasLost 0
                person2 hasLost 0
            }
        }
    }

    @Eac("A failed transfer will keep all value accounted for in the world", """
        The sum of all purses' balances and lost components does not change.
        Atomic transfer always leads to abort (no ignore path) taken.
    """)
    fun transferFailedImpliesAllValueAccounted() {
        given {
            thereIsAWorld(world1) {
                thereIsAPurse(person1, 3)
                thereIsAPurse(person2, 2)
            }
        }
        then {
            world1 hasTotalValue 5
            world1 hasTotalBalance 5
            world1 hasTotalLost 0
        }
        successor {
            whenever {
                makeATransfer(person1, person2, 2, FAILURE)
            }
            then {
                // SP1: balance after <= balance before (some lost); SP2: total value remains teh same;
                world1 hasTotalValue 5
                world1 hasTotalBalance 3
                world1 hasTotalLost 2
                person1 hasBalance 1
                person2 hasBalance 2
                person1 hasLost 2
                person2 hasLost 0
            }
        }
    }
}
