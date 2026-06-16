package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDerivedQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQueryWithDummy
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.dsl.DerivedQueryBlock
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayQueries
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayVerify
import kotlin.collections.emptyList
import kotlin.reflect.KFunction

/**
 * Root class for the three types of query factory (queries in query position, queries in verification position,
 * and queries in derived position).
 */
abstract class AbstractRightOfWayScriptGenerationQueryFactory() : RightOfWayQueryFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (RightOfWayQueryFactory) -> Query<C>, deriveQuery: (T, RightOfWayQueryFactory) -> DerivedQuery<*>
    ): DerivedQuery<T> {
        // TODO: is there any way to avoid using a dummy here?
        val genQueryWithDummy =
            derivedFrom(RightOfWayScriptGenerationDerivedQueryFactory) as ScriptGenerationQueryWithDummy<C, T>
        val derivation = deriveQuery(genQueryWithDummy.dummy, this) as ScriptGenerationDerivedQuery<*>

        return ScriptGenerationDerivedQuery {
            """
            forAll({
                ${genQueryWithDummy.getQueryScript()}
            }, {
                ${derivation.getDerivedQueryScript()}
            })
            """
        }
    }

    override fun <T> liftQueriesToDerivedQuery(queries: List<Query<*>>): DerivedQuery<T> =
        ScriptGenerationDerivedQuery {
            // The DSL doesn't expose this function directly, it just has a variant of `forAll` that takes a query.
            queries.joinToString("\n") { (it as ScriptGenerationQuery<*>).getQueryScript() }
        }
}

abstract class RightOfWayScriptGenerationQueryFactory(val queryPosition: QueryPosition) :
    AbstractRightOfWayScriptGenerationQueryFactory() {

    override fun allAircraftsIn(airspaceName: String) =
        query<Aircrafts>(QueryReference.AllAircraftsIn, airspaceName)
    override fun airspaceHasAircraft(airspaceName: String, aircraftName: String) =
        query<Boolean>(QueryReference.AirspaceHasAircraft, airspaceName, aircraftName)
    override fun hasRightOfWay(airspaceName: String) =
        query<Set<Pair<String, String>>>(QueryReference.HasRightOfWayAll, airspaceName)
    override fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) =
        query<Boolean>(QueryReference.HasRightOfWaySpecific, airspaceName, aircraft0, aircraft1)

    private fun <T> query(reference: QueryReference, vararg args: Any?) = reference.inPosition(queryPosition)?.let {
        RightOfWayScriptGenerationQuery<T> {
            RightOfWayScriptingHelper.scriptifyFunction(it, *args)
        }
    } ?: UnsupportedQuery()
}

// One specific object per query type
object RightOfWayScriptGenerationQueryQueryFactory : RightOfWayScriptGenerationQueryFactory(QueryPosition.Query)

object RightOfWayScriptGenerationVerificationQueryFactory : RightOfWayScriptGenerationQueryFactory(QueryPosition.Verify)

// TODO LF: why isn't QueryPosition.Derived a val here?
object RightOfWayScriptGenerationDerivedQueryFactory : AbstractRightOfWayScriptGenerationQueryFactory() {

//    override fun airspaceHasAircraft(airspaceName: String, aircraftName: String) =
//        query<List<Boolean>, Boolean>(QueryReference.AirspaceHasAircraft, false, airspaceName, aircraftName)
//    override fun hasRightOfWay(airspaceName: String) =
//        query<List<Set<Pair<String, String>>>, Set<Pair<String, String>>>(QueryReference.HasRightOfWayAll, emptySet(), airspaceName)
//    override fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) =
//        query<List<Boolean>, Boolean>(QueryReference.HasRightOfWaySpecific, false, airspaceName, aircraft0, aircraft1)

    private fun <C : Collection<T>, T> query(reference: QueryReference, dummy: T, vararg args: Any?) =
        reference.inPosition(QueryPosition.Derived)?.let {
            ScriptGenerationQueryWithDummy(RightOfWayScriptGenerationQuery<C> {
                RightOfWayScriptingHelper.scriptifyFunction(it, *args)
            }, dummy)
        } ?: UnsupportedQuery()
}

enum class QueryPosition {
    Query, Verify, Derived
}

//TODO LF: this looks way too complicated for the cast; but other alternatives I could find have failed
fun <T : Function<*>> forceFnType(fn: T) = fn as KFunction<*>
val hasRightOfWayAllQ = forceFnType<RightOfWayQueries.(String) -> Unit>(RightOfWayQueries::hasRightOfWay)
val hasRightOfWaySpecificQ = forceFnType<RightOfWayQueries.(String, String, String) -> Unit>(RightOfWayQueries::hasRightOfWay)
val hasRightOfWayAllV = forceFnType<RightOfWayVerify.(String) -> Unit>(RightOfWayVerify::hasRightOfWay)
val hasRightOfWaySpecificV = forceFnType<RightOfWayVerify.(String, String, String) -> Unit>(RightOfWayVerify::hasRightOfWay)
val hasRightOfWayAllD = forceFnType<DerivedQueryBlock.(String) -> RightOfWayQueryFactory.() -> Query<Set<Pair<String, String>>>>(DerivedQueryBlock::hasRightOfWay)
val hasRightOfWaySpecificD = forceFnType<DerivedQueryBlock.(String, String, String) -> RightOfWayQueryFactory.() -> Query<Boolean>>(DerivedQueryBlock::hasRightOfWay)
//    private val hasRightOfWayAll: (String) -> Unit = RightOfWayQueries::hasRightOfWay
//    private val hasRightOfWayAll = { airspaceName: String -> RightOfWayQueries::hasRightOfWay(airspaceName) }
//    private val hasRightOfWayAll: kotlin.reflect.KFunction2<RightOfWayQueries, String, Unit> = RightOfWayQueries::hasRightOfWay
//((RightOfWayQueries::hasRightOfWay as (RightOfWayQueries, String) -> Unit)) as kotlin.reflect.KFunction2<RightOfWayQueries, String, Unit>),//KFunction1<String, Unit>,//(String) -> Unit,

enum class QueryReference(
    val inQueryPosition: KFunction<*>, val inVerificationPosition: KFunction<*>, val inDerivedPosition: KFunction<*>?
) {

    AllAircraftsIn(RightOfWayQueries::allAircraftsIn,
        RightOfWayVerify::airspaceHasAircraft,
        DerivedQueryBlock::allAircraftsIn),
    AirspaceHasAircraft(RightOfWayQueries::airspaceHasAircraft,
        RightOfWayVerify::airspaceHasAircraft,
        DerivedQueryBlock::airspaceHasAircraft),
    HasRightOfWayAll(hasRightOfWayAllQ,
        hasRightOfWayAllV,
        hasRightOfWayAllD),
    HasRightOfWaySpecific(hasRightOfWaySpecificQ,
        hasRightOfWaySpecificV,
        hasRightOfWaySpecificD);

    fun inPosition(position: QueryPosition) = when (position) {
        QueryPosition.Query -> inQueryPosition
        QueryPosition.Verify -> inVerificationPosition
        QueryPosition.Derived -> inDerivedPosition
    }
}

fun interface RightOfWayScriptGenerationQuery<T> : ScriptGenerationQuery<T> {

    override val behavior get() = unsupportedBehavior
}
