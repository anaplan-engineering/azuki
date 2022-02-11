package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.runner.RunnableScenario
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation

open class TicTacToeScenario(implementation: TicTacToeImplementation) :
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
        TicTacToeDslProvider,
        implementation)

