package com.anaplan.engineering.azuki.core.scenario

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface BuildableScenario<AF : ActionFactory> {
    fun buildActions(actionFactory: AF): List<Action>
    fun definitions(actionFactory: AF): List<Action>
}

abstract class AbstractBuildableScenario<
    AF : ActionFactory,
    G : Given<AF>,
    W : When<AF>
    >(
    protected open val dslProvider: DslProvider<AF, *, *, *, G, W, *, *, *, *, *>
) {

    var givenFunction: (G.() -> Unit)? = null
    var whenFunction: (W.() -> Unit)? = null

    fun given(givenFunction: G.() -> Unit) {
        this.givenFunction = givenFunction
    }

    fun whenever(whenFunction: W.() -> Unit) {
        this.whenFunction = whenFunction
    }

    open fun definitions(actionFactory: AF): List<Action> {
        val given = dslProvider.createGiven(actionFactory)
        givenFunction?.let { given.it() }
        return given.actions()
    }

    open fun buildActions(actionFactory: AF): List<Action> {
        val whenever = dslProvider.createWhen(actionFactory)
        whenFunction?.let { whenever.it() }
        return whenever.actions()
    }

}
