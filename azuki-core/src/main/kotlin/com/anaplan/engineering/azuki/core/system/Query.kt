package com.anaplan.engineering.azuki.core.system

interface QueryFactory

object NoQueryFactory : QueryFactory

/**
 * Basic framework for creating query factories that make RunnableQueries and RunnableDerivedQueries.
 */
abstract class RunnableQueryFactory<E, CF : CheckFactory> : QueryFactory {

    /**
     * Narrows a Query to a RunnableQuery.
     */
    protected fun <T> Query<T>.ensureRunnable() = let {
        require(this is RunnableQuery<*, *, T>) {
            if (this is UnsupportedQuery) {
                "Query is unsupported"
            } else {
                "Query is incorrect class: ${this::class.simpleName}"
            }
        }
        @Suppress("UNCHECKED_CAST") (this as RunnableQuery<E, CF, T>)
    }

    /**
     * Narrows a DerivedQuery to a RunnableDerivedQuery.
     */
    protected fun <T> DerivedQuery<T>.ensureRunnable() = let {
        require (this is RunnableDerivedQuery<*, *, T>) {
            "Derived query is incorrect class: ${it::class.simpleName}"
        }
        @Suppress("UNCHECKED_CAST") (this as RunnableDerivedQuery<E, CF, T>)
    }

    /**
     * Lifts a list of queries to a derived query.
     */
    fun <T> thereIs(queries: List<Query<*>>) = ListRunnableDerivedQuery<E, CF, T>(queries.map { it.ensureRunnable() })
}

interface Query<T> : ReifiedBehavior

interface DerivedQuery<T>

/**
 * A query that can be run on an environment.
 */
interface RunnableQuery<E, CF : CheckFactory, T> : Query<T> {

    /**
     * Run a query on an environment to return an answer.
     */
    fun run(environment: E): Answer<T, CF>
}

/**
 * A derived query that can be run on an environment.
 */
fun interface RunnableDerivedQuery<E, CF : CheckFactory, T> : DerivedQuery<T> {

    /**
     * Derives a list of queries from this derived query based on the state of the environment.
     */
    fun derive(environment: E): List<RunnableQuery<E, CF, *>>
}

/**
 * Adapts a list of runnable queries into a derived query.
 *
 * The main use of this adapter is to form the final layer of a nested quantification.
 */
data class ListRunnableDerivedQuery<E, CF : CheckFactory, T>(val queries: List<RunnableQuery<E, CF, *>>) :
    RunnableDerivedQuery<E, CF, T> {

    override fun derive(environment: E) = queries
}

/**
 * Implements a runnable for-all query.
 *
 * The answers from the driver query will be used to produce another layer of derivation, which will then be derived,
 * and so on until we reach a base case (such as a ListRunnableDerivedQuery).
 */
class ForallRunnableDerivedQuery<E, CF : CheckFactory, T, C : Collection<T>>(
    val driver: RunnableQuery<E, CF, C>, val derivedQueryFactory: (T) -> RunnableDerivedQuery<E, CF, *>
) : RunnableDerivedQuery<E, CF, T> {

    override fun derive(environment: E): List<RunnableQuery<E, CF, *>> {
        val answers = driver.run(environment).value
        return answers.flatMap { t -> derivedQueryFactory(t).derive(environment) }
    }
}


class UnsupportedQuery<T> : Query<T> {

    override val behavior = unsupportedBehavior
}

interface Answer<T, CF : CheckFactory> {
    val to: Query<T>
    val value: T
    fun createChecks(factory: CF): List<Check>
}

/**
 * With a validatable answer, the validation checks are used to ensure that the answer is sufficient, while the standard
 * checks are used for verifying that a system continues to return the same answer to a query.
 */
interface ValidatableAnswer<T, CF : CheckFactory> : Answer<T, CF> {
    fun createValidationChecks(factory: CF): List<Check>
}
