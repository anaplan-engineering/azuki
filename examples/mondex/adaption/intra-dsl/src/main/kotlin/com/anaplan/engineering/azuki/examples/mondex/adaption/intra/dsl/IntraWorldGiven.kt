package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.definition.PurseDefinition
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.definition.TransferDefinition
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.definition.WorldDefinition
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl.ActionBlock
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl.WorldActionBlock

class IntraWorldGiven(actionFactory: IntraWorldActionFactory<*>) :
    ActionBlock(actionFactory),
    Given<IntraWorldActionFactory<*>>,
    PurseDefinition by WorldActionBlock(actionFactory, null),
    TransferDefinition by WorldActionBlock(actionFactory, null),
    WorldDefinition {

    override fun thereIsAWorld(worldName: String, init: (WorldActionBlock.() -> Unit)?) {
        add(
            WorldActionBlock(actionFactory, worldName).apply {
                if (init != null) {
                    init()
                }
            }.actions()
        )
    }

}
