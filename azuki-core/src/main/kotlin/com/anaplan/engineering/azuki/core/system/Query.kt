package com.anaplan.engineering.azuki.core.system

interface QueryFactory

object NoQueryFactory : QueryFactory

interface Query<T> : ReifiedBehavior

interface DerivedQuery<T>

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
