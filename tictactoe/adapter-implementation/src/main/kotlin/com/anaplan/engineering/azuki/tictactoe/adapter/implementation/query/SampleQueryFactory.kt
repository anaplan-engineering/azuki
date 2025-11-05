package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.query

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import org.slf4j.LoggerFactory

class SampleQueryFactory : TicTacToeQueryFactory {

    override fun getToken(gameName: String, position: Position) = object : SampleQuery<String?> {
        override fun run(env: ExecutionEnvironment): SampleAnswer<String?> {
            val playerName = env.withGame(gameName) {
                tokenAt(x = position.col, y = position.row)?.symbol
            }

            return SampleAnswer(this, playerName) {
                listOf(if (playerName == null) {
                    it.game.hasSpace(gameName, position)
                } else {
                    it.game.hasToken(gameName, playerName, position)
                })
            }
        }
    }
}

fun interface SampleQuery<T> : Query<T> {

    override val behavior get() = unsupportedBehavior

    fun run(env: ExecutionEnvironment): SampleAnswer<T>
}

class SampleDerivedQuery<T, C : Collection<T>>(
    val driver: Query<C>, val derivedQueryFactory: (T) -> List<Query<*>>
) : DerivedQuery<T> {

    // TODO - shouldn't be filtering out unsupported queries here -- should be detecting earlier
    fun derive(env: ExecutionEnvironment) = when (driver) {
        is UnsupportedQuery -> {
            Log.error("Driver is unsupported in derived query")
            emptyList()
        }
        // TODO -- this should be different for for some.. don't map every value
        is SampleQuery<C> -> driver.run(env).value.flatMap { t ->
            derivedQueryFactory(t).mapNotNull {
                when (it) {
                    is UnsupportedQuery<*> -> {
                        Log.warn("Derived query is unsupported")
                        null
                    }

                    is SampleQuery<*> -> it
                    else -> throw IllegalStateException("derived query is incorrect class: ${it::class.simpleName}")
                }
            }
        }

        else -> throw IllegalStateException("driver query is incorrect class: ${driver::class.simpleName}")
    }

    companion object {

        private val Log = LoggerFactory.getLogger(SampleDerivedQuery::class.java)
    }
}
