package com.anaplan.engineering.azuki.core.system

/**
 * A query that can be run on an environment.
 */
interface RunnableQuery<E, CF : CheckFactory, T> : Query<T> {

    /**
     * Runs this query on an environment, returning an answer in terms of the state of the environment.
     */
    fun run(environment: E): Answer<T, CF>
}

/**
 * Narrows a Query to a RunnableQuery.
 */
fun <E, CF : CheckFactory, T> Query<T>.ensureRunnable(): RunnableQuery<E, CF, T> {
    require(this is RunnableQuery<*, *, T>) { "Query is " + if (this is UnsupportedQuery) "unsupported" else "not runnable: ${this::class.simpleName}" }
    @Suppress("UNCHECKED_CAST") return this as RunnableQuery<E, CF, T>
}

/**
 * A derived query that can be applied to an environment to derive runnable queries.
 */
fun interface RunnableDerivedQuery<E, CF : CheckFactory, T> : DerivedQuery<T> {

    /**
     * Derives a list of queries based on the state of the environment.
     */
    fun derive(environment: E): List<RunnableQuery<E, CF, *>>
}

/**
 * Adapts a list of runnable queries into a derived query.
 *
 * The main use of this adapter is to form the final layer of a nested quantification.
 */
data class RunnableQueriesAsDerivedQuery<E, CF : CheckFactory, T>(val queries: List<RunnableQuery<E, CF, *>>) :
    RunnableDerivedQuery<E, CF, T> {

    override fun derive(environment: E) = queries

    companion object {

        fun <E, CF : CheckFactory, T> fromQueries(queries: List<Query<*>>) =
            RunnableQueriesAsDerivedQuery<E, CF, T>(queries.map { it.ensureRunnable() })
    }
}

/**
 * Implements a runnable for-all query.
 *
 * Derives queries recursively until we reach a base case (such as `RunnableQueriesAsDerivedQuery`).
 */
class ForAllRunnableDerivedQuery<E, CF : CheckFactory, T, C : Collection<T>>(
    val driver: RunnableQuery<E, CF, C>, val derivedQueryFactory: (T) -> RunnableDerivedQuery<E, CF, *>
) : RunnableDerivedQuery<E, CF, T> {

    override fun derive(environment: E): List<RunnableQuery<E, CF, *>> {
        val answers = driver.run(environment).value
        return answers.flatMap { t -> derivedQueryFactory(t).derive(environment) }
    }
}
