package com.anaplan.engineering.azuki.tictactoe.dsl

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
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory

open class TicTacToeScenario :
    RunnableScenario<
        TicTacToeActionFactory,
        TicTacToeCheckFactory,
        NoQueryFactory,
        NoActionGeneratorFactory,
        TicTacToeGiven,
        TicTacToeWhen,
        TicTacToeThen,
        NoVerify,
        NoQueries,
        NoGenerate,
        TicTacToeRegardlessOf,
        NoSystemDefaults>(
        TicTacToeDslProvider)

interface TicTacToeBuildableScenario : BuildableScenario<TicTacToeActionFactory> {
    fun given(givenFunction: TicTacToeGiven.() -> Unit)
    fun whenever(whenFunction: TicTacToeWhen.() -> Unit)
}

interface TicTacToeVerifiableScenario : VerifiableScenario<TicTacToeActionFactory, TicTacToeCheckFactory>,
    TicTacToeBuildableScenario {
    fun then(thenFunction: TicTacToeThen.() -> Unit)
}

open class TicTacToeVerifiableScenarioImpl :
    AbstractVerifiableScenario<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeGiven, TicTacToeWhen, TicTacToeThen, TicTacToeRegardlessOf>(
        TicTacToeDslProvider), TicTacToeVerifiableScenario

fun verifiableScenario(init: TicTacToeVerifiableScenario.() -> Unit): TicTacToeVerifiableScenario {
    val scenario = TicTacToeVerifiableScenarioImpl()
    scenario.init()
    return scenario
}
