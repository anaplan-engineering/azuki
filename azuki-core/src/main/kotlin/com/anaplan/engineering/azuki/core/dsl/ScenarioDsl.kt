package com.anaplan.engineering.azuki.core.dsl

import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.*

@DslMarker
annotation class ScenarioDsl

class DuplicateGivenExpression : IllegalStateException()
class DuplicateWhenExpression : IllegalStateException()
class DuplicateThenExpression : IllegalStateException()

@ScenarioDsl
interface Given<AF : ActionFactory> {
    fun actions(): List<Action>
}

@ScenarioDsl
interface When<AF : ActionFactory> {
    fun actions(): List<Action>
}

@ScenarioDsl
interface Then<CF : CheckFactory> {
    fun checks(): List<Check>
}

@ScenarioDsl
interface Verify<QF : QueryFactory> {
    fun queries(): ScenarioQueries
}

@ScenarioDsl
interface Queries<QF : QueryFactory> {
    fun queries(): ScenarioQueries
}

@ScenarioDsl
interface RegardlessOf<AF : ActionFactory> {
    fun actions(): List<Action>
}

@ScenarioDsl
interface Generate<AGF : ActionGeneratorFactory> {
    fun generators(): List<ActionGenerator>
}

object NoQueries : Queries<NoQueryFactory> {
    override fun queries() = throw UnsupportedOperationException("This implementation does not support querying")
}

object NoVerify : Verify<NoQueryFactory> {
    override fun queries() = throw UnsupportedOperationException("This implementation does not support verification")
}

object NoGenerate : Generate<NoActionGeneratorFactory> {
    override fun generators() = throw UnsupportedOperationException("This implementation does not support generation")
}

interface DslProvider<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    G : Given<AF>,
    W : When<AF>,
    T : Then<CF>,
    V : Verify<QF>,
    Q : Queries<QF>,
    R : Generate<AGF>,
    RO: RegardlessOf<AF>,
    > {
    fun createGiven(actionFactory: AF): G
    fun createWhen(actionFactory: AF): W
    fun createThen(checkFactory: CF): T
    fun createVerify(queryFactory: QF): V
    fun createQueries(queryFactory: QF): Q
    fun createGenerate(actionGeneratorFactory: AGF): R
    fun createRegardlessOf(actionFactory: AF): RO
}
