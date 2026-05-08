package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.runner.RunnableScenario
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
