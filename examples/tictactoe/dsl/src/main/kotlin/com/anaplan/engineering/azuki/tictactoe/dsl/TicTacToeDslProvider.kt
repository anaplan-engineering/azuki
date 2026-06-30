package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

object TicTacToeDslProvider :
    DslProvider<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, TicTacToeGiven, TicTacToeWhen, TicTacToeThen, TicTacToeVerify, TicTacToeQueries, TicTacToeGenerate, TicTacToeRegardlessOf> {

    override fun createGiven(actionFactory: TicTacToeActionFactory) = TicTacToeGiven(actionFactory)

    override fun createWhen(actionFactory: TicTacToeActionFactory) = TicTacToeWhen(actionFactory)

    override fun createThen(checkFactory: TicTacToeCheckFactory) = TicTacToeThen(checkFactory)

    override fun createVerify(queryFactory: TicTacToeQueryFactory) = TicTacToeVerify(queryFactory)

    override fun createQueries(queryFactory: TicTacToeQueryFactory) = TicTacToeQueries(queryFactory)

    override fun createGenerate(actionGeneratorFactory: TicTacToeActionGeneratorFactory) = TicTacToeGenerate(actionGeneratorFactory)

    override fun createRegardlessOf(actionFactory: TicTacToeActionFactory) = TicTacToeRegardlessOf(actionFactory)

}
