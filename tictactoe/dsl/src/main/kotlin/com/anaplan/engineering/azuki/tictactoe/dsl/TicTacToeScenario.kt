package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.runner.RunnableScenario
import com.anaplan.engineering.azuki.core.scenario.AbstractOracleScenario
import com.anaplan.engineering.azuki.core.scenario.AbstractQueryScenario
import com.anaplan.engineering.azuki.core.scenario.AbstractVerifiableScenario
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.scenario.ScenarioWithQueries
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

open class TicTacToeScenario :
    RunnableScenario<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, TicTacToeGiven, TicTacToeWhen, TicTacToeThen, TicTacToeVerify, TicTacToeQueries, TicTacToeGenerate, TicTacToeRegardlessOf, NoSystemDefaults>(
        TicTacToeDslProvider)

interface TicTacToeBuildableScenario : BuildableScenario<TicTacToeActionFactory> {

    fun given(givenFunction: TicTacToeGiven.() -> Unit)
    fun whenever(whenFunction: TicTacToeWhen.() -> Unit)
}

interface TicTacToeVerifiableScenario : VerifiableScenario<TicTacToeActionFactory, TicTacToeCheckFactory>,
    TicTacToeBuildableScenario {

    fun then(thenFunction: TicTacToeThen.() -> Unit)
}

interface TicTacToeOracleScenario :
    OracleScenario<TicTacToeActionFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>,
    TicTacToeBuildableScenario {

    fun generate(generationFunction: TicTacToeGenerate.() -> Unit)
    fun verify(verifyFunction: TicTacToeVerify.() -> Unit)
}

interface TicTacToeQueryScenario : ScenarioWithQueries<TicTacToeActionFactory, TicTacToeQueryFactory> {

    fun query(queryFunction: TicTacToeQueries.() -> Unit)
}

open class TicTacToeVerifiableScenarioImpl :
    AbstractVerifiableScenario<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeGiven, TicTacToeWhen, TicTacToeThen, TicTacToeRegardlessOf>(
        TicTacToeDslProvider), TicTacToeVerifiableScenario

open class TicTacToeOracleScenarioImpl :
    AbstractOracleScenario<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, TicTacToeGiven, TicTacToeWhen, TicTacToeThen, TicTacToeVerify, TicTacToeQueries, TicTacToeGenerate>(
        TicTacToeDslProvider), TicTacToeOracleScenario

open class TicTacToeQueryScenarioImpl :
    AbstractQueryScenario<TicTacToeActionFactory, TicTacToeQueryFactory, TicTacToeGiven, TicTacToeWhen, TicTacToeQueries>(
        TicTacToeDslProvider), TicTacToeQueryScenario

fun verifiableScenario(init: TicTacToeVerifiableScenario.() -> Unit): TicTacToeVerifiableScenario {
    val scenario = TicTacToeVerifiableScenarioImpl()
    scenario.init()
    return scenario
}

fun oracleScenario(init: TicTacToeOracleScenario.() -> Unit) = TicTacToeOracleScenarioImpl().apply(init)

fun queryScenario(init: TicTacToeQueryScenario.() -> Unit) = TicTacToeQueryScenarioImpl().apply(init)
