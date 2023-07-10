package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.runner.RunnableScenario
import com.anaplan.engineering.azuki.core.scenario.AbstractVerifiableScenario
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory

open class GraphScenario : RunnableScenario<
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
    GraphRegardlessOf,
    NoSystemDefaults>(GraphDslProvider)

interface GraphBuildableScenario : BuildableScenario<GraphActionFactory> {
    fun given(givenFunction: GraphGiven.() -> Unit)
    fun whenever(whenFunction: GraphWhen.() -> Unit)
}

interface GraphVerifiableScenario : VerifiableScenario<GraphActionFactory, GraphCheckFactory>,
    GraphBuildableScenario {
    fun then(thenFunction: GraphThen.() -> Unit)
}

open class GraphVerifiableScenarioImpl :
    AbstractVerifiableScenario<GraphActionFactory, GraphCheckFactory, GraphGiven, GraphWhen, GraphThen, GraphRegardlessOf>(
        GraphDslProvider), GraphVerifiableScenario

fun verifiableScenario(init: GraphVerifiableScenario.() -> Unit): GraphVerifiableScenario {
    val scenario = GraphVerifiableScenarioImpl()
    scenario.init()
    return scenario
}
