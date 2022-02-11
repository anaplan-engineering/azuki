package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.DslProvider
import com.anaplan.engineering.azuki.core.dsl.NoGenerate
import com.anaplan.engineering.azuki.core.dsl.NoQueries
import com.anaplan.engineering.azuki.core.dsl.NoVerify
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory

object TicTacToeDslProvider :
    DslProvider<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, TicTacToeGiven, TicTacToeWhen, TicTacToeThen, NoVerify, NoQueries, NoGenerate, TicTacToeRegardlessOf> {

    override fun createGiven(actionFactory: TicTacToeActionFactory) = TicTacToeGiven(actionFactory)

    override fun createWhen(actionFactory: TicTacToeActionFactory) = TicTacToeWhen(actionFactory)

    override fun createThen(checkFactory: TicTacToeCheckFactory) = TicTacToeThen(checkFactory)

    override fun createVerify(queryFactory: NoQueryFactory) = NoVerify

    override fun createQueries(queryFactory: NoQueryFactory) = NoQueries

    override fun createGenerate(actionGeneratorFactory: NoActionGeneratorFactory) = NoGenerate

    override fun createRegardlessOf(actionFactory: TicTacToeActionFactory) = TicTacToeRegardlessOf(actionFactory)

}
