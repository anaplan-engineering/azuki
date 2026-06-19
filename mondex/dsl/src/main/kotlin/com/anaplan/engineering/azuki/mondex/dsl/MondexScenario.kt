package com.anaplan.engineering.azuki.mondex.dsl

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
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory

open class MondexScenario : RunnableScenario<
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
    MondexRegardlessOf,
    NoSystemDefaults>(MondexDslProvider)

interface MondexBuildableScenario : BuildableScenario<MondexActionFactory<*>> {
    fun given(givenFunction: MondexGiven.() -> Unit)
    fun whenever(whenFunction: MondexWhen.() -> Unit)
}

interface MondexVerifiableScenario : VerifiableScenario<MondexActionFactory<*>, MondexCheckFactory>,
    MondexBuildableScenario {
    fun then(thenFunction: MondexThen.() -> Unit)
}

open class MondexVerifiableScenarioImpl :
    AbstractVerifiableScenario<MondexActionFactory<*>, MondexCheckFactory, MondexGiven, MondexWhen, MondexThen, MondexRegardlessOf>(
        MondexDslProvider), MondexVerifiableScenario

fun verifiableScenario(init: MondexVerifiableScenario.() -> Unit): MondexVerifiableScenario {
    val scenario = MondexVerifiableScenarioImpl()
    scenario.init()
    return scenario
}
