package com.anaplan.engineering.azuki.core.scenario

import com.anaplan.engineering.azuki.core.dsl.*
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

/**
 * A scenario that will be verified by determining whether checks are satisfied by one system
 */
interface VerifiableScenario<AF : ActionFactory, CF : CheckFactory> : BuildableScenario<AF> {
    fun checks(checkFactory: CF): List<Check>
    fun regardlessOfActions(actionFactory: AF): List<List<Action>>
}

abstract class AbstractVerifiableScenario<
    AF : ActionFactory,
    CF : CheckFactory,
    G : Given<AF>,
    W : When<AF>,
    T : Then<CF>,
    RO: RegardlessOf<AF>,
    >(
    override val dslProvider: DslProvider<AF, CF, *, *, G, W, T, *, *, *, RO>
) : AbstractBuildableScenario<AF, G, W>(dslProvider), VerifiableScenario<AF, CF> {


    private var thenFunction: (T.() -> Unit)? = null

    fun then(thenFunction: T.() -> Unit) {
        this.thenFunction = thenFunction
    }

    override fun checks(checkFactory: CF): List<Check> {
        val then = dslProvider.createThen(checkFactory)
        thenFunction?.let { then.it() }
        return then.checks()
    }

    private val regardlessOfFunctions: MutableList<RO.() -> Unit> = mutableListOf()

    fun regardlessOf(regardlessOfFunction: RO.() -> Unit) {
        regardlessOfFunctions.add(regardlessOfFunction)
    }

    override fun regardlessOfActions(actionFactory: AF) =
        regardlessOfFunctions.map {
            val regardlessOf = dslProvider.createRegardlessOf(actionFactory)
            regardlessOf.apply { it() }.actions()
        }

}
