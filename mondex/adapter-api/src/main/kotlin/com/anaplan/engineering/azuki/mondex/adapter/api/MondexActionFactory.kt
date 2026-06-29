package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ParallelActionFactory


//LF: @QST I think here might be helpful to distinguish the given x when parts, since in Mondex, given is creating the world
//    and whenever is running the protocol? For clarity/separation of concerns?
interface MondexActionFactory<out A: Action > : ActionFactory, ParallelActionFactory<A> {
    val purse: PurseActionFactory
    val world: WorldActionFactory
}

interface PurseActionFactory {
    fun create(purseName: String, purse: Purse): Action
}

interface WorldActionFactory {
    fun create(authPurses: Map<String, Purse>): Action
    fun transferOkay(transferDetails: TransferDetails): Action
    fun transferLost(transferDetails: TransferDetails): Action
    fun noTransfer(): Action
}
