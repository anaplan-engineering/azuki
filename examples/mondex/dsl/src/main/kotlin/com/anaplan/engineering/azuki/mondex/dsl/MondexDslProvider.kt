package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory

object MondexDslProvider : DslProvider<
    MondexActionFactory<*>,
    MondexCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    MondexGiven,
    MondexWhen,
    MondexThen,
    NoVerify,
    NoQueries,
    NoGenerate,
    MondexRegardlessOf
    > {
    override fun createGiven(actionFactory: MondexActionFactory<*>) = MondexGiven(actionFactory)

    override fun createWhen(actionFactory: MondexActionFactory<*>) = MondexWhen(actionFactory)

    override fun createThen(checkFactory: MondexCheckFactory) = MondexThen(checkFactory)

    override fun createVerify(queryFactory: NoQueryFactory) = NoVerify

    override fun createQueries(queryFactory: NoQueryFactory) = NoQueries

    override fun createGenerate(actionGeneratorFactory: NoActionGeneratorFactory) = NoGenerate

    override fun createRegardlessOf(actionFactory: MondexActionFactory<*>) = MondexRegardlessOf(actionFactory)
}
