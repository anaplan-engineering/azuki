package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Verify
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory

class RightOfWayVerify(private val queryFactory: RightOfWayQueryFactory) : Verify<RightOfWayQueryFactory> {

    fun <T, C : Collection<T>> forAll(
        getDriver: DerivedQueryBlock.() -> (qf: RightOfWayQueryFactory) -> Query<C>, v: RightOfWayVerify.(T) -> Unit
    ) {
        derivedQueriesList.add(queryFactory.createForAllQuery(DerivedQueryBlock().getDriver()) { t: T, qf: RightOfWayQueryFactory ->
            val verify = RightOfWayVerify(qf).apply { v(t) }

            // TODO: relax these requirements by creating a derived query concatenation operator?
            require(verify.queriesList.isEmpty() || verify.derivedQueriesList.isEmpty()) { "forAll cannot have both derived and non-derived queries" }
            require(verify.derivedQueriesList.size <= 1) { "forAll cannot have more than one nested derived query" }

            if (verify.queriesList.isNotEmpty()) {
                qf.liftQueriesToDerivedQuery<T>(verify.queriesList)
            } else {
                verify.derivedQueriesList[0]
            }
        })
    }

    fun hasRightOfWay(airspaceName: String) =
        addQuery { hasRightOfWay(airspaceName) }
    fun hasRightOfWay(airspaceName: String, withRightOfWay: String, givingWay: String) =
        addQuery { hasRightOfWay(airspaceName, withRightOfWay, givingWay) }
    fun allAircraftsIn(airspaceName: String) =
        addQuery { allAircraftsIn(airspaceName) }
    fun airspaceHasAircraft(airspaceName: String, aircraftName: String) =
        addQuery { airspaceHasAircraft(airspaceName, aircraftName) }

    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    private fun addQuery(via: RightOfWayQueryFactory.() -> Query<*>) {
        queriesList.add(queryFactory.via())
    }
}
