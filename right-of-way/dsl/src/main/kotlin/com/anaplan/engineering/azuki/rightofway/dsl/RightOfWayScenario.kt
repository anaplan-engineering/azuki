package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.runner.RunnableScenario
import com.anaplan.engineering.azuki.core.scenario.AbstractOracleScenario
import com.anaplan.engineering.azuki.core.scenario.AbstractQueryScenario
import com.anaplan.engineering.azuki.core.scenario.AbstractVerifiableScenario
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.scenario.ScenarioWithQueries
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory

open class RightOfWayRunnableScenario :
    RunnableScenario<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory,
        RightOfWayActionGeneratorFactory, RightOfWayGiven, RightOfWayWhen, RightOfWayThen,
        RightOfWayVerify, RightOfWayQueries, RightOfWayGenerate, RightOfWayRegardlessOf,
        NoSystemDefaults>(RightOfWayDslProvider)

interface RightOfWayBuildableScenario : BuildableScenario<RightOfWayActionFactory> {
    fun given(givenFunction: RightOfWayGiven.() -> Unit)
    fun whenever(whenFunction: RightOfWayWhen.() -> Unit)
}

interface RightOfWayVerifiableScenario :
    VerifiableScenario<RightOfWayActionFactory, RightOfWayCheckFactory>,
    RightOfWayBuildableScenario {

    fun then(thenFunction: RightOfWayThen.() -> Unit)
}

interface RightOfWayOracleScenario :
    OracleScenario<RightOfWayActionFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory>,
    RightOfWayBuildableScenario {

    fun generate(generationFunction: RightOfWayGenerate.() -> Unit)
    fun verify(verifyFunction: RightOfWayVerify.() -> Unit)
}

interface RightOfWayQueryScenario : ScenarioWithQueries<RightOfWayActionFactory, RightOfWayQueryFactory>,
    RightOfWayBuildableScenario {

    fun query(queryFunction: RightOfWayQueries.() -> Unit)
}

open class RightOfWayVerifiableScenarioImpl :
    AbstractVerifiableScenario<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayGiven, RightOfWayWhen, RightOfWayThen, RightOfWayRegardlessOf>(
        RightOfWayDslProvider), RightOfWayVerifiableScenario

open class RightOfWayOracleScenarioImpl :
    AbstractOracleScenario<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory, RightOfWayGiven, RightOfWayWhen, RightOfWayThen, RightOfWayVerify, RightOfWayQueries, RightOfWayGenerate>(
        RightOfWayDslProvider), RightOfWayOracleScenario

open class RightOfWayQueryScenarioImpl :
    AbstractQueryScenario<RightOfWayActionFactory, RightOfWayQueryFactory, RightOfWayGiven, RightOfWayWhen, RightOfWayQueries>(
        RightOfWayDslProvider), RightOfWayQueryScenario

fun verifiableScenario(init: RightOfWayVerifiableScenario.() -> Unit): RightOfWayVerifiableScenario {
    val scenario = RightOfWayVerifiableScenarioImpl()
    scenario.init()
    return scenario
}

fun oracleScenario(init: RightOfWayOracleScenario.() -> Unit) = RightOfWayOracleScenarioImpl().apply(init)

fun queryScenario(init: RightOfWayQueryScenario.() -> Unit) = RightOfWayQueryScenarioImpl().apply(init)
