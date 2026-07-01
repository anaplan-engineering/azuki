package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.AbPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.ConPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.Status
import com.anaplan.engineering.azuki.mondex.dsl.block.AbWorldBlock
import com.anaplan.engineering.azuki.mondex.dsl.block.ConWorldBlock
import com.anaplan.engineering.azuki.mondex.dsl.declaration.ModexDeclarations
import org.slf4j.LoggerFactory

class MondexGiven(private val actionFactory: MondexActionFactory<*>) : Given<MondexActionFactory<*>>,
    ModexDeclarations {

    companion object {
        private val Log = LoggerFactory.getLogger(MondexGiven::class.java)
    }

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList

    override fun thereIsAnAbstractWorld(name: String, init: AbWorldBlock.() -> Unit) {
        val block = AbWorldBlock(actionFactory)
        block.init()
        actionList.add(actionFactory.world.createAbWorld(name, block.abPurses))
    }

    override fun thereIsAConcreteWorld(name: String, init: ConWorldBlock.() -> Unit) {
        val block = ConWorldBlock(actionFactory)
        block.init()
        actionList.add(actionFactory.world.createConWorld(name, block.conPurses))
    }
}
