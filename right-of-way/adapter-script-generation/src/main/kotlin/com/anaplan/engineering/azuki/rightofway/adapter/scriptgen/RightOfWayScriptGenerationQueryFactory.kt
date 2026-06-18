package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDerivedQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQueryWithDummy
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.dsl.DerivedQueryBlock
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayQueries
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayVerify
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction4

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
    override fun hasRightOfWay(airspaceName: String, withRightOfWay: String, givingWay: String) =
        query<Boolean>(QueryReference.HasRightOfWaySpecific, airspaceName, withRightOfWay, givingWay)

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

private typealias DerivedHasRightOfWayAll =
    RightOfWayQueryFactory.() -> Query<Set<Pair<String, String>>>
private typealias DerivedHasRightOfWaySpecific = RightOfWayQueryFactory.() -> Query<Boolean>

private object HasRightOfWayScriptRefs {
    val queryAll: KFunction2<RightOfWayQueries, String, Unit> = RightOfWayQueries::hasRightOfWay
    val querySpecific: KFunction4<RightOfWayQueries, String, String, String, Unit> = RightOfWayQueries::hasRightOfWay
    val verifyAll: KFunction2<RightOfWayVerify, String, Unit> = RightOfWayVerify::hasRightOfWay
    val verifySpecific: KFunction4<RightOfWayVerify, String, String, String, Unit> = RightOfWayVerify::hasRightOfWay
    val derivedAll: KFunction2<DerivedQueryBlock, String, DerivedHasRightOfWayAll> = DerivedQueryBlock::hasRightOfWay
    val derivedSpecific: KFunction4<DerivedQueryBlock, String, String, String, DerivedHasRightOfWaySpecific> =
        DerivedQueryBlock::hasRightOfWay
}

enum class QueryReference(
    val inQueryPosition: KFunction<*>, val inVerificationPosition: KFunction<*>, val inDerivedPosition: KFunction<*>?
) {

    AllAircraftsIn(RightOfWayQueries::allAircraftsIn,
        RightOfWayVerify::allAircraftsIn,
        DerivedQueryBlock::allAircraftsIn),
    AirspaceHasAircraft(RightOfWayQueries::airspaceHasAircraft,
        RightOfWayVerify::airspaceHasAircraft,
        DerivedQueryBlock::airspaceHasAircraft),
    HasRightOfWayAll(HasRightOfWayScriptRefs.queryAll,
        HasRightOfWayScriptRefs.verifyAll,
        HasRightOfWayScriptRefs.derivedAll),
    HasRightOfWaySpecific(HasRightOfWayScriptRefs.querySpecific,
        HasRightOfWayScriptRefs.verifySpecific,
        HasRightOfWayScriptRefs.derivedSpecific);

    fun inPosition(position: QueryPosition) = when (position) {
        QueryPosition.Query -> inQueryPosition
        QueryPosition.Verify -> inVerificationPosition
        QueryPosition.Derived -> inDerivedPosition
    }
}

fun interface RightOfWayScriptGenerationQuery<T> : ScriptGenerationQuery<T> {

    override val behavior get() = unsupportedBehavior
}
