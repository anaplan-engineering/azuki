package com.anaplan.engineering.azuki.examples.mondex.specification.abstract

import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbSystem_Module.mk_AbSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.Transfer_Module.mk_Transfer
import com.anaplan.engineering.kazuki.core.*

@Module
interface AbSystem {

    val abWorld: AbWorld

    @FunctionProvider(AbSystemFunctions::class)
    val functions: AbSystemFunctions

}

class AbSystemFunctions(val system: AbSystem) {

    fun update(update: (AbWorld) -> AbWorld) = updateSystem(update(system.abWorld))

    private val updateSystem = function(
        command = { after: AbWorld -> mk_AbSystem(after) },
        // pre = { after -> after != system.abWorld },  // this is allowed
        post = { after, result ->
            SecurityProperties.noValueCreated(system.abWorld, result.abWorld) &&
                SecurityProperties.allValueAccounted(system.abWorld, result.abWorld)
        }
    )

    val ignore = function(
        command = { aIn: AIn -> update { w -> w.functions.abIgnore(aIn)._1 } }
    )

    val successfulTransfer = function(
        command = { td: TransferDetails -> update { w -> w.functions.abTransferOkayTD(mk_Transfer(td), td)._1 } },
    )

    val lostTransfer = function(
        command = { td: TransferDetails -> update { w -> w.functions.abTransferLostTD(mk_Transfer(td), td)._1 } }
    )
}

fun createSystem() = mk_AbSystem(AbInitState())



