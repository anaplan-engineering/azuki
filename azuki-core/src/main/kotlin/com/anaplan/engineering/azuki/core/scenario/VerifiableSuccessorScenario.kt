package com.anaplan.engineering.azuki.core.scenario

import com.anaplan.engineering.azuki.core.dsl.*
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

class VerifiableSuccessorScenario<
    AF : ActionFactory,
    CF : CheckFactory,
    W : When<AF>,
    T : Then<CF>,
    RO : RegardlessOf<AF>,
    >(
    private val dslProvider: DslProvider<AF, CF, *, *, *, W, T, *, *, *, RO>
): VerifiableScenarioIteration<AF, CF> {

    private var thenFunction: (T.() -> Unit)? = null
    private var whenFunction: (W.() -> Unit)? = null

    fun whenever(whenFunction: W.() -> Unit) {
        this.whenFunction = whenFunction
    }

    override fun commands(actionFactory: AF): List<Action> {
        val whenever = dslProvider.createWhen(actionFactory)
        whenFunction?.let { whenever.it() }
        return whenever.actions()
    }


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
