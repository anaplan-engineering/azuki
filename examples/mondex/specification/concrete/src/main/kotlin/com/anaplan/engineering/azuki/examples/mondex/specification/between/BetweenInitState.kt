package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld_Module.mk_BetweenWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook_Module.mk_LogBook
import com.anaplan.engineering.kazuki.core.*

/**
 * PRG126 Sect 6.1:
 * We do not want to model adding new authentic purses to the
 * system, since some of the operations involved are outside the security
 * boundary.  So we allow the world to be `switched off' and a new world
 * `switched on', where the new world consists of the old world as it
 * was, plus the new purses.  So our initial state must allow purses to
 * be part-way through transactions.
 *
 * We set constraints on the initial state of the between system to say
 * that there are all the request messages in the $ether$, any current
 * transactions must be valid, and there are no future messages.
 *
 * Here we create an Betweenworld with empty ether.
 */
//TODO LF will populate the ether as we receive transfers
val BetweenInitState = function<BetweenWorld>(
    command = { mk_BetweenWorld(mk_InjectiveMapping(), mk_Set(), mk_LogBook()) }
)
