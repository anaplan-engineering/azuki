package com.anaplan.engineering.azuki.examples.mondex.specification.concrete

import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConSystem_Module.mk_ConSystem
import com.anaplan.engineering.kazuki.core.*

@Module
interface ConSystem {

    val conWorld: ConWorld

    @FunctionProvider(ConSystemFunctions::class)
    val functions: ConSystemFunctions
}

class ConSystemFunctions(val system: ConSystem) {

    fun update(update: (ConWorld) -> ConWorld) = updateSystem(update(system.conWorld))

    private val updateSystem = function(
        command = { after: ConWorld -> mk_ConSystem(after) },
        // pre = { after -> after != system.conWorld },  // this is allowed
    )

    val startTransfer = function(
        command = { td: TransferDetails ->
            update { w ->
                // TODO - SF - I have basically made this up and feel like this is a missing part of the spec and prob doesn't belong here
                val fromPurse = w.conAuthPurse[td.from]
                val toPurse = w.conAuthPurse[td.to]

                val fromMsg = Message.StartFrom(
                    mk_CounterPartyDetails(
                        toPurse.name,
                        td.value,
                        toPurse.nextSeqNo
                    )
                )
                val toMsg = Message.StartTo(
                    mk_CounterPartyDetails(
                        fromPurse.name,
                        td.value,
                        fromPurse.nextSeqNo
                    )
                )

                val resultFrom = fromPurse.functions.startFromPurseOkay(fromMsg)
                val resultTo = toPurse.functions.startToPurseOkay(toMsg)

                w.transform(
                    conAuthPurse = w.conAuthPurse * mk_InjectiveMapping(
                        mk_(td.from, resultFrom._1),
                        mk_(td.to, resultFrom._2),
                    ),
                    // TODO - certainly not sure where the messages go!
                    ether = w.ether + resultFrom._2 + resultTo._2
                )
            }
        },
    )

}
