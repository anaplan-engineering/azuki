package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl

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
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory

open class IntraWorldScenario : RunnableScenario<
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
    IntraWorldRegardlessOf,
    NoSystemDefaults>(IntraWorldDslProvider)

interface IntraWorldBuildableScenario : BuildableScenario<IntraWorldActionFactory<*>> {
    fun given(givenFunction: IntraWorldGiven.() -> Unit)
    fun whenever(whenFunction: IntraWorldWhen.() -> Unit)
}

interface IntraWorldVerifiableScenario : VerifiableScenario<IntraWorldActionFactory<*>, IntraWorldCheckFactory>,
    IntraWorldBuildableScenario {
    fun then(thenFunction: IntraWorldThen.() -> Unit)
}

open class IntraWorldVerifiableScenarioImpl :
    AbstractVerifiableScenario<IntraWorldActionFactory<*>, IntraWorldCheckFactory, IntraWorldGiven, IntraWorldWhen, IntraWorldThen, IntraWorldRegardlessOf>(
        IntraWorldDslProvider), IntraWorldVerifiableScenario

fun verifiableScenario(init: IntraWorldVerifiableScenario.() -> Unit): IntraWorldVerifiableScenario {
    val scenario = IntraWorldVerifiableScenarioImpl()
    scenario.init()
    return scenario
}
