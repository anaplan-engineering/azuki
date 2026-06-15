package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory

object RightOfWayDslProvider :
    DslProvider<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory,
        RightOfWayActionGeneratorFactory, RightOfWayGiven, RightOfWayWhen, RightOfWayThen,
        RightOfWayVerify, RightOfWayQueries, RightOfWayGenerate, RightOfWayRegardlessOf> {

    override fun createGiven(actionFactory: RightOfWayActionFactory) = RightOfWayGiven(actionFactory)

    override fun createWhen(actionFactory: RightOfWayActionFactory) = RightOfWayWhen(actionFactory)

    override fun createThen(checkFactory: RightOfWayCheckFactory) = RightOfWayThen(checkFactory)

    override fun createVerify(queryFactory: RightOfWayQueryFactory) = RightOfWayVerify(queryFactory)

    override fun createQueries(queryFactory: RightOfWayQueryFactory) = RightOfWayQueries(queryFactory)

    override fun createGenerate(actionGeneratorFactory: RightOfWayActionGeneratorFactory) =
        RightOfWayGenerate(actionGeneratorFactory)

    override fun createRegardlessOf(actionFactory: RightOfWayActionFactory) = RightOfWayRegardlessOf(actionFactory)

}
