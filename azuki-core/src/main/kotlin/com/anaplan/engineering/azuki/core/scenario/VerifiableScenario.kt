package com.anaplan.engineering.azuki.core.scenario

import com.anaplan.engineering.azuki.core.dsl.*
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

interface VerifiableScenarioIteration<AF : ActionFactory, CF : CheckFactory> {
    fun commands(actionFactory: AF): List<Action>
    fun checks(checkFactory: CF): List<Check>
    fun regardlessOfActions(actionFactory: AF): List<List<Action>>
}

/**
 * A scenario that will be verified by determining whether checks are satisfied by one system
 */
interface VerifiableScenario<AF : ActionFactory, CF : CheckFactory> : BuildableScenario<AF>,
    VerifiableScenarioIteration<AF, CF> {
    fun iterations(): List<VerifiableScenarioIteration<AF, CF>>
}

abstract class AbstractVerifiableScenario<
    AF : ActionFactory,
    CF : CheckFactory,
    G : Given<AF>,
    W : When<AF>,
    T : Then<CF>,
    RO : RegardlessOf<AF>,
    >(
    override val dslProvider: DslProvider<AF, CF, *, *, G, W, T, *, *, *, RO>
) : AbstractBuildableScenario<AF, G, W>(dslProvider), VerifiableScenario<AF, CF> {


    private var thenFunction: (T.() -> Unit)? = null
    private val successorFunctions = mutableListOf<VerifiableSuccessorScenario<AF, CF, W, T, RO>.() -> Unit>()

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

    // TODO -- separate indexed successors or just have index for all?
    fun successors(vararg successorFunctions: VerifiableSuccessorScenario<AF, CF, W, T, RO>.() -> Unit) {
        this.successorFunctions.addAll(successorFunctions)
    }

    fun successor(successorFunction: VerifiableSuccessorScenario<AF, CF, W, T, RO>.() -> Unit) =
        successors(successorFunction)

    /**
     * Calls the successor function `count` times with an incrementing argument starting at 1
     */
    fun repeat(count: Int, successorFunction: VerifiableSuccessorScenario<AF, CF, W, T, RO>.(Int) -> Unit) {
        successorFunctions.addAll((1..count).map { i ->
            { s: VerifiableSuccessorScenario<AF, CF, W, T, RO> ->
                s.successorFunction(i)
            }
        })
    }

    override fun iterations() =
        if (whenFunction == null && thenFunction == null) {
            emptyList()
        } else {
            listOf(this)
        } + successorFunctions.map {
            VerifiableSuccessorScenario(dslProvider).apply(it)
        }
}
