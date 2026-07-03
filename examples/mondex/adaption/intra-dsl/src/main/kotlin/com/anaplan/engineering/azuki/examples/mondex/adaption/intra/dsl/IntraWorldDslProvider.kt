package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory

object IntraWorldDslProvider : DslProvider<
    IntraWorldActionFactory<*>,
    IntraWorldCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    IntraWorldGiven,
    IntraWorldWhen,
    IntraWorldThen,
    NoVerify,
    NoQueries,
    NoGenerate,
    IntraWorldRegardlessOf
    > {
    override fun createGiven(actionFactory: IntraWorldActionFactory<*>) = IntraWorldGiven(actionFactory)

    override fun createWhen(actionFactory: IntraWorldActionFactory<*>) = IntraWorldWhen(actionFactory)

    override fun createThen(checkFactory: IntraWorldCheckFactory) = IntraWorldThen(checkFactory)

    override fun createVerify(queryFactory: NoQueryFactory) = NoVerify

    override fun createQueries(queryFactory: NoQueryFactory) = NoQueries

    override fun createGenerate(actionGeneratorFactory: NoActionGeneratorFactory) = NoGenerate

    override fun createRegardlessOf(actionFactory: IntraWorldActionFactory<*>) = IntraWorldRegardlessOf(actionFactory)
}
