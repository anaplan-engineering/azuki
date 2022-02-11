package com.anaplan.engineering.azuki.core.scenario

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.dsl.Queries
import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryFactory

/**
 * A scenario that will give a list of answers to queries
 */

interface ScenarioWithQueries<AF: ActionFactory, QF: QueryFactory> : BuildableScenario<AF> {

    fun queries(queryFactory: QF): ScenarioQueries

}

abstract class AbstractQueryScenario<
    AF: ActionFactory,
    QF: QueryFactory,
    G: Given<AF>,
    W: When<AF>,
    Q: Queries<QF>,
    >(override val dslProvider: DslProvider<AF, *, QF, *, G, W, *, *, Q, *, *>) : ScenarioWithQueries<AF, QF>, AbstractBuildableScenario<AF, G, W>(dslProvider) {

    private var queryFunction: (Q.() -> Unit)? = null

    fun query(queryFunction: Q.() -> Unit) {
        this.queryFunction = queryFunction
    }

    override fun queries(queryFactory: QF): ScenarioQueries {
        val queries = dslProvider.createQueries(queryFactory)
        queryFunction?.let { queries.it() }
        return queries.queries()
    }

}

data class ScenarioQueries(
    val queries: List<Query<*>>,
    val forAllQueries: List<DerivedQuery<*>>
) {
    fun isEmpty() = queries.isEmpty() && forAllQueries.isEmpty()
}

