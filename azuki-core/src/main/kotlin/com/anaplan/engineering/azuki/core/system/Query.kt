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
