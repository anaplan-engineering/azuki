package com.anaplan.engineering.azuki.examples.mondex.intra.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldFunctionalElements
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.IntraWorldScenario
import com.anaplan.engineering.azuki.mondex.person1
import com.anaplan.engineering.azuki.mondex.person2

@BEH(1, IntraWorldFunctionalElements.World, """
    Create a world with authentic purses
""")
class BEH1 : IntraWorldScenario() {

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
            thereIsAPurse(person1, 3)
            thereIsAPurse(person2, 3)
        }
        whenever {
            makeATransfer(person1, person2, 1)
        }
        then {
            person1 hasBalance 2
            person2 hasBalance 4
        }
    }

//    @Eac("Mondex abstract one run")
//    fun mondexAbWorldOneRun() {
//        given {
//            // Mondex run 1
//            // AbWorld = { "leo" |-> AbPurse(10,0), "erin" |-> AbPurse(20,0) }
//            thereIsAPurse("leo", 10, 0)
//            thereIsAPurse("erin", 20, 0)
//        }
//        whenever {
//            //LF @EK if the DSL is mixed, now you will need a way to distinguish between the purse concrete x abstract calls everywhere....
//            // AbWorld  = { "leo" |-> AbPurse(10,0), "erin" |-> AbPurse(20,0) }
//            thereIsATransfer("erin", "leo", 5)
//            // AbWorld' = { "leo" |-> AbPurse(15,0), "erin" |-> AbPurse(15,0) }
//        }
//        then {
//            // Sanity checks
//            purseExists("leo", 15, 0)
//            purseExists("erin", 15, 0)
//
//            //LF @EK might matter here to know which world you are operating under, so something like...
//            //    here defaults to ABSTRACT on the inner calls.
//            //    if the DSL is together, there is a lot care to ensure that the level of check is in the right place
//            //    (e.g. worldExists(ABSTRACT) { noValueCreation(BETWEEN) } is a refinement check not a sanity one)
//            worldExists {
//                // Mondex SP : totalValue world = 30 = 10 + 20 <= totalValue world' = 30 = 15 + 15
//                noValueCreation()
//                // Mondex SP: totalValue world + totalLost world = totalValue world' + totalLost world'
//                allValuesAccountedFor()
//            }
//        }
//    }

//    @Eac("Mondex concrete one run")
//    fun mondexConWorldOneRun() {
//        given {
//            // Mondex run 1
//            // ConWorld = { "leo" |-> ConPurse(10,{},"leo", 0, pd.., eaFrom),
//            //              "erin" |-> ConPurse(20,{},"erin", 0, pd.., eaFrom) }
//            //LF @QST You will need a spurious PD to start with? Maybe no need to have it as a parameter?
//            thereIsAPurse("leo", 10U, emptySet(), 0U,
//                // last payment details was from leo to erin of zero (i.e. spurious bootstrapping). Needs to add some elegance
//                PayDetails(TransferDetails("leo", "erin", 0U), 0U, 0U), Status.eaFrom)
//            thereIsAPurse("erin", 20U, emptySet(), 0U,
//                PayDetails(TransferDetails("leo", "erin", 0U), 0U, 0U), Status.eaFrom)
//        }
//        whenever {
//            //LF @QST if the DSL is mixed, now you will need a way to distinguish between the purse concrete x abstract calls everywhere....
//            //    this now needs a way of changing the context of what a transfer is
//            //thereIsATransfer("erin", "leo", 5)
//        }
//        then {
//            // Sanity checks now also might need some kind of purse level (i.e. I expect a ConPurse to exist here)
//            purseExists("leo", 15, 0)
//            purseExists("erin", 15, 0)
//
//            //LF @EK you might want between world checks? doesn't make sense in the abstract. No-Op for abstract?
//            //    there will be loads of such no ops for abstract as you go down the chain
//            //
//            // * ConPurse related sanity checks
//            //      * startFromPurseOkay, startToPurseOkay, etc. (PRG sect 4.7.1)
//            //      * preparing promotion to inject ConPurse in ConWorld, will neeed startFromEaFromPurseOkay (PRG sect 4.9.1)
//            //      * note that startFromPurseOkay may abort, whereas startFromEaFromPurseOkay is success case
//
//            //LF @EK between world SPs
//            worldExists(WorldLevel.BETWEEN) {
//                // Mondex SP : totalValue world = 30 = 10 + 20 <= totalValue world' = 30 = 15 + 15
//                noValueCreation()
//                // Mondex SP: totalValue world + totalLost world = totalValue world' + totalLost world'
//                allValuesAccountedFor()
//            }
//
//            //LF @QST could we implement refinement like?
//            worldExists(ABSTRACT) {
//                // This would require the retrieve to implicit, might want to have the retrieve Rab etc (PRG sect 10.1)
//                noValueCreation(WorldLevel.BETWEEN)
//            }
//        }
//    }

//    @Eac("Mondex twice run")
//    fun mondexTwice() {
//        given {
//            // World = { "leo" |-> AbPurse(10,0), "erin" |-> AbPurse(20,0) }
//            thereIsAPurse("leo", 10, 0)
//            thereIsAPurse("erin", 20, 0)
//        }
//        whenever {
//            // Muiltiple runs don't quite make sense in Mondex, but for Azuki you could see it as showing
//            // that it works on multiple protocol runs (i.e. Mondex is forall)
//
//            // Mondex run 1
//            // World = { "leo" |-> AbPurse(10,0), "erin" |-> AbPurse(20,0) }
//            thereIsATransfer("erin", "leo", 5)
//            // World' = { "leo" |-> AbPurse(15,0), "erin" |-> AbPurse(15,0) }
//
//            // Mondex run 2
//            // World' = { "leo" |-> AbPurse(15,0), "erin" |-> AbPurse(15,0) }
//            thereIsATransfer("leo", "erin", 10)
//            // World'' = { "leo" |-> AbPurse(5,0), "erin" |-> AbPurse(25,0) }
//        }
//        then {
//
//        }
//    }
}
