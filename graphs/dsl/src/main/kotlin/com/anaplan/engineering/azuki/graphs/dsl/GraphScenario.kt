package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.runner.RunnableScenario
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
