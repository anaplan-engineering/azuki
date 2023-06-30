package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory

object GraphDslProvider : DslProvider<
    GraphActionFactory,
    GraphCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    GraphGiven,
    GraphWhen,
    GraphThen,
    NoVerify,
    NoQueries,
    NoGenerate,
    GraphRegardlessOf
    > {
    override fun createGiven(actionFactory: GraphActionFactory) = GraphGiven(actionFactory)

    override fun createWhen(actionFactory: GraphActionFactory) = GraphWhen(actionFactory)

    override fun createThen(checkFactory: GraphCheckFactory) = GraphThen(checkFactory)

    override fun createVerify(queryFactory: NoQueryFactory) = NoVerify

    override fun createQueries(queryFactory: NoQueryFactory) = NoQueries

    override fun createGenerate(actionGeneratorFactory: NoActionGeneratorFactory) = NoGenerate

    override fun createRegardlessOf(actionFactory: GraphActionFactory) = GraphRegardlessOf(actionFactory)
}

