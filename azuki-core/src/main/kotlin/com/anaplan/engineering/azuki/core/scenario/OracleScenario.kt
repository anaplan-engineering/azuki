package com.anaplan.engineering.azuki.core.scenario

import com.anaplan.engineering.azuki.core.dsl.*
import com.anaplan.engineering.azuki.core.system.*

/**
 * A scenario that will be verified by determining whether answers to queries in one system satisfy the checks of another.
 */
interface OracleScenario<
    AF : ActionFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory
    >
    : ScenarioWithQueries<AF, QF> {
    fun actionGenerations(actionGeneratorFactory: AGF): List<List<ActionGenerator>>
}

open class AbstractOracleScenario<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    G : Given<AF>,
    W : When<AF>,
    T : Then<CF>,
    V : Verify<QF>,
    Q : Queries<QF>,
    R : Generate<AGF>
    >(
    override val dslProvider: DslProvider<AF, CF, QF, AGF, G, W, T, V, Q, R, *>
) : OracleScenario<AF, QF, AGF>, AbstractBuildableScenario<AF, G, W>(dslProvider) {

    private var verificationFunction: (V.() -> Unit)? = null

    fun verify(verificationFunction: V.() -> Unit) {
        this.verificationFunction = verificationFunction
    }

    private val generationFunctions: MutableList<R.() -> Unit> = mutableListOf()

    fun generate(generationFunction: (R.() -> Unit)) {
        generationFunctions.add(generationFunction)
    }

    override fun actionGenerations(actionGeneratorFactory: AGF) =
        generationFunctions.map {
            val generate = dslProvider.createGenerate(actionGeneratorFactory)
            generate.apply { it() }.generators()
        }

    override fun queries(queryFactory: QF): ScenarioQueries {
        val verify = dslProvider.createVerify(queryFactory)
        verificationFunction?.let { verify.it() }
        return verify.queries()
    }

}



