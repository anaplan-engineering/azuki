package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ParallelActionFactory


interface MondexActionFactory<out A: Action > : ActionFactory, ParallelActionFactory<A> {

}
