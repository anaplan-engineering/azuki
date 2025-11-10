package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDerivedQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQueryWithDummy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.dsl.DerivedQueryBlock
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeQueries
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeVerify
import kotlin.reflect.KFunction

/**
 * Root class for the three types of query factory (queries in query position, queries in verification position,
 * and queries in derived position).
 */
abstract class AbstractTicTacToeScriptGenerationQueryFactory() : TicTacToeQueryFactory {

    override fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>, deriveQuery: (T, TicTacToeQueryFactory) -> List<Query<*>>
    ) = createDerivedQuery(QueryOperator.ForAll, derivedFrom, deriveQuery)

    override fun <T, C : Collection<T>> createForSomeQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>, deriveQuery: (T, TicTacToeQueryFactory) -> List<Query<*>>
    ) = createDerivedQuery(QueryOperator.ForSome, derivedFrom, deriveQuery)

    @Suppress("UNCHECKED_CAST")
    private fun <T, C : Collection<T>> createDerivedQuery(
        operator: QueryOperator,
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>, deriveQuery: (T, TicTacToeQueryFactory) -> List<Query<*>>
    ): DerivedQuery<T> {
        val genQueryWithDummy =
            derivedFrom(TicTacToeScriptGenerationDerivedQueryFactory) as ScriptGenerationQueryWithDummy<C, T>
        val derivedFromScript = genQueryWithDummy.getQueryScript()
        val derivationScript =
            (deriveQuery(genQueryWithDummy.dummy,
                this).map { (it as ScriptGenerationQuery<T>).getQueryScript() }).joinToString("\n")

        return TicTacToeScriptGenerationDerivedQuery(
            operator,
            derivedFromScript,
            derivationScript,
        )
    }
}

abstract class TicTacToeScriptGenerationQueryFactory(val queryPosition: QueryPosition) : AbstractTicTacToeScriptGenerationQueryFactory() {

    override fun getGames() = query<List<String>>(QueryReference.Games)
    override fun getPositions(gameName: String) = query<List<Position>>(QueryReference.Positions, gameName)
    override fun getWidth(gameName: String) = query<Int>(QueryReference.Width, gameName)
    override fun getHeight(gameName: String) = query<Int>(QueryReference.Height, gameName)
    override fun getToken(gameName: String, position: Position) = query<String?>(QueryReference.Token, gameName, position)

    private fun<T> query(reference: QueryReference, vararg args: Any?) =
        reference.inPosition(queryPosition)?.let {
            TicTacToeScriptGenerationQuery<T> {
                TicTacToeScriptingHelper.scriptifyFunction(it, *args)
            }
        } ?: UnsupportedQuery()
}

object TicTacToeScriptGenerationQueryQueryFactory: TicTacToeScriptGenerationQueryFactory(QueryPosition.Query)
object TicTacToeScriptGenerationVerificationQueryFactory: TicTacToeScriptGenerationQueryFactory(QueryPosition.Verify)

object TicTacToeScriptGenerationDerivedQueryFactory : AbstractTicTacToeScriptGenerationQueryFactory() {

    override fun getGames() = query<List<String>, String>(QueryReference.Games, "")
    override fun getPositions(gameName: String) = query<List<Position>, Position>(QueryReference.Positions, Position(-1, -1), gameName)

    private fun<C : Collection<T>, T> query(reference: QueryReference, dummy: T, vararg args: Any?) =
        reference.inPosition(QueryPosition.Derived)?.let {
            ScriptGenerationQueryWithDummy(
                TicTacToeScriptGenerationQuery<C> {
                    TicTacToeScriptingHelper.scriptifyFunction(it, *args)
                },
                dummy
            )
        } ?: UnsupportedQuery()
}

enum class QueryPosition {
    Query,
    Verify,
    Derived
}

enum class QueryReference(val inQueryPosition: KFunction<*>, val inVerificationPosition: KFunction<*>, val inDerivedPosition: KFunction<*>?) {
    Games(TicTacToeQueries::getGames, TicTacToeVerify::hasGames, DerivedQueryBlock::getGames),
    Width(TicTacToeQueries::getWidth, TicTacToeVerify::gameHasWidth, DerivedQueryBlock::getWidth),
    Height(TicTacToeQueries::getHeight, TicTacToeVerify::gameHasHeight, DerivedQueryBlock::getHeight),
    Positions(TicTacToeQueries::getPositions, TicTacToeVerify::gameHasPositions, DerivedQueryBlock::getPositions),
    Token(TicTacToeQueries::getToken, TicTacToeVerify::gameHasToken, null);

    fun inPosition(position: QueryPosition) = when(position) {
        QueryPosition.Query -> inQueryPosition
        QueryPosition.Verify -> inVerificationPosition
        QueryPosition.Derived -> inDerivedPosition
    }
}

class TicTacToeScriptGenerationDerivedQuery<T>(
    val operator: QueryOperator, val derivedFromScript: String, val derivationScript: String
) : ScriptGenerationDerivedQuery<T> {

    override fun getDerivedQueryScript() =
        """
            ${operator.script}({
                $derivedFromScript
            }, {
                $derivationScript
            })
        """
}

enum class QueryOperator(val script: String) {
    ForAll("forAll"), ForSome("forSome")
}

fun interface TicTacToeScriptGenerationQuery<T> : ScriptGenerationQuery<T> {

    override val behavior get() = unsupportedBehavior
}
