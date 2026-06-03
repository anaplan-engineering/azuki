package com.anaplan.engineering.azuki.rightofway.adapter.implementation.query

import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory

open class SampleAnswer<T> (
    override val to: Query<T>,
    override val value: T,
    private val checkCreator: (TicTacToeCheckFactory) -> List<Check>
) : Answer<T, TicTacToeCheckFactory> {

    override fun createChecks(factory: TicTacToeCheckFactory) = checkCreator(factory)
    override fun toString() = "Answer: $value"
}
