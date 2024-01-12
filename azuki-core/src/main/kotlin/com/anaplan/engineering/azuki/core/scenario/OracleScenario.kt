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
    fun givenActionGenerations(actionGeneratorFactory: AGF): List<List<ActionGenerator>>
    fun whenActionGenerations(actionGeneratorFactory: AGF): List<List<ActionGenerator>>
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

    private val givenGenerationFunctions: MutableList<R.() -> Unit> = mutableListOf()
    private val whenGenerationFunctions: MutableList<R.() -> Unit> = mutableListOf()

    // Generate actions will be associated with the given block if no whenever
    // blocks have been previously declared.
    // Otherwise, they will be associated with the whenever block.
    fun generate(generationFunction: (R.() -> Unit)) {
        if (whenFunction == null) {
            givenGenerationFunctions.add(generationFunction)
        } else {
            whenGenerationFunctions.add(generationFunction)
        }
    }

    override fun givenActionGenerations(actionGeneratorFactory: AGF) =
        givenGenerationFunctions.map {
            val generate = dslProvider.createGenerate(actionGeneratorFactory)
            generate.apply { it() }.generators()
        }

    override fun whenActionGenerations(actionGeneratorFactory: AGF) =
        whenGenerationFunctions.map {
            val generate = dslProvider.createGenerate(actionGeneratorFactory)
            generate.apply { it() }.generators()
        }

    override fun queries(queryFactory: QF): ScenarioQueries {
        val verify = dslProvider.createVerify(queryFactory)
        verificationFunction?.let { verify.it() }
        return verify.queries()
    }

}



