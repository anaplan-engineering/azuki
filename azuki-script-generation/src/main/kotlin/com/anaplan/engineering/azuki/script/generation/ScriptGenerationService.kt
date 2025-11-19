package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.*

/**
 * The main endpoint for script generation tasks.
 */
class ScriptGenerationService<
    // mandatory parameters
    out AF : ActionFactory,
    out CF : CheckFactory,
    DS : DeclarationState,
    // optional parameters (these require calling 'withXYZFactory')
    E : ScriptGenerationEnvironment,
    out QF : QueryFactory,
    out AGF : ActionGeneratorFactory,
    > private constructor(
    // mandatory parameters
    internal val actionFactory: AF,
    internal val checkFactory: CF,
    internal val declarationStateFactory: DeclarationStateFactory<DS>,
    // optional parameters
    internal val environmentFactory: ScriptGenerationEnvironmentFactory<E>,
    internal val queryQueryFactory: QF,
    internal val verifyQueryFactory: QF,
    internal val actionGeneratorFactory: AGF,
) {

    /**
     * Provides ready-made usage patterns for script generation.
     */
    val patterns = ScriptGenerationPatterns(this)

    /**
     * Adds an environment factory to this service, changing the type of accepted scenarios accordingly.
     */
    fun <N : ScriptGenerationEnvironment> withEnvironmentFactory(new: ScriptGenerationEnvironmentFactory<N>) =
        ScriptGenerationService(
            actionFactory,
            checkFactory,
            declarationStateFactory,
            environmentFactory = new,
            queryQueryFactory,
            verifyQueryFactory,
            actionGeneratorFactory,
        )

    /**
     * Adds query factories to this service, changing the type of accepted scenarios accordingly.
     */
    fun <N : QueryFactory> withQueryFactories(query: N, verify: N) = ScriptGenerationService(
        actionFactory,
        checkFactory,
        declarationStateFactory,
        environmentFactory,
        queryQueryFactory = query,
        verifyQueryFactory = verify,
        actionGeneratorFactory,
    )

    /**
     * Adds an action generation factory to this service, changing the type of accepted scenarios accordingly.
     */
    fun <N : ActionGeneratorFactory> withActionGeneratorFactory(new: N) = ScriptGenerationService(
        actionFactory,
        checkFactory,
        declarationStateFactory,
        environmentFactory,
        queryQueryFactory,
        verifyQueryFactory,
        actionGeneratorFactory = new,
    )

    /**
     * Constructs a given block by mixing in declarations from one or more sources.
     */
    fun given(body: GivenBuilder<AF, DS, E>.() -> Unit) = environmentFactory.create().let { e ->
        Given(e, GivenBuilder(actionFactory, declarationStateFactory, e).build(body))
    }

    companion object {

        /**
         * Constructs a basic script generator factory with no optional extras included.
         */
        fun <AF : ActionFactory, CF : CheckFactory, DS : DeclarationState> create(
            actionFactory: AF, checkFactory: CF, declarationStateFactory: DeclarationStateFactory<DS>
        ) = ScriptGenerationService(
            actionFactory,
            checkFactory,
            declarationStateFactory,
            environmentFactory = { NoScriptGenerationEnvironment },
            NoQueryFactory,
            NoQueryFactory,
            NoActionGeneratorFactory,
        )
    }

    inner class Given(internal val environment: E, scriptFragments: List<String>) :
        BasicScriptBlock("given", scriptFragments) {

        /**
         * Constructs a whenever block by mixing in declarations from one or more sources.
         */
        fun whenever(body: WheneverBuilder<AF, E>.() -> Unit) =
            GivenWhenever(this, WheneverBuilder(actionFactory, environment).build(body))

        /**
         * Constructs a given-generate block by mixing in generators from one or more sources.
         */
        fun generate(body: GenerateBuilder<AF, QF, AGF>.() -> Unit) =
            GivenGenerate(this, GivenGenerateBuilder<AF, QF, AGF>(actionGeneratorFactory).build(body))
    }

    abstract inner class Whenever(scriptFragments: List<String>) : BasicScriptBlock("whenever", scriptFragments) {

        abstract val given: Given
        protected val environment get() = given.environment
    }

    /**
     * A whenever block following a given block.
     */
    inner class GivenWhenever(override val given: Given, scriptFragments: List<String>) : Whenever(scriptFragments) {

        /**
         * Finishes generation of an incomplete verifiable scenario.
         */
        fun incompleteScenario() = IncompleteScenarioScript(given, whenever = this)

        /**
         * Constructs a then block by mixing in checks from one or more sources.
         */
        fun then(build: ThenBuilder<CF, E>.() -> Unit) =
            GivenWheneverThen(this, ThenBuilder(checkFactory, environment).apply(build).scriptFragments)

        /**
         * Constructs a query block by mixing in queries from one or more sources.
         */
        fun query(build: QueryBuilder<QF, E>.() -> Unit) =
            GivenWheneverQuery(this, QueryBuilder(queryQueryFactory, environment).apply(build).scriptFragments)
    }

    inner class GivenWheneverThen(val whenever: GivenWhenever, scriptFragments: List<String>) :
        BasicScriptBlock("then", scriptFragments) {

        internal val given get() = whenever.given

        /**
         * Constructs a verifiable scenario script with the given, when, and then blocks previously constructed.
         */
        fun verifiableScenario() = VerifiableScenarioScript(given, whenever, then = this)
    }

    inner class GivenWheneverQuery(val whenever: GivenWhenever, scriptFragments: List<String>) :
        BasicScriptBlock("query", scriptFragments) {

        val given = whenever.given

        /**
         * Constructs a query scenario script with the given, when, and query blocks previously constructed.
         */
        fun queryScenario() = QueryScenarioScript(given, whenever, query = this)
    }

    /**
     * A list of generate blocks in 'given' position.
     */
    inner class GivenGenerate(val given: Given, subBlocks: List<ScriptBlock>) : CompositeScriptBlock(subBlocks) {

        /**
         * Constructs a whenever block by mixing in declarations from one or more sources.
         */
        fun whenever(body: WheneverBuilder<AF, E>.() -> Unit) =
            GivenGenerateWhenever(this, WheneverBuilder(actionFactory, environment).build(body))

        private val environment = given.environment
    }

    /**
     * A whenever block following a given-generate block.
     */
    inner class GivenGenerateWhenever(val givenGenerate: GivenGenerate, scriptFragments: List<String>) :
        Whenever(scriptFragments) {

        override val given = givenGenerate.given

        /**
         * Constructs a whenever-generate block by mixing in generators from one or more sources.
         */
        fun generate(body: GenerateBuilder<AF, QF, AGF>.() -> Unit) = GivenGenerateWheneverGenerate(this,
            WheneverGenerateBuilder<AF, QF, AGF>(actionGeneratorFactory).build(body))
    }

    /**
     * A list of generate blocks in 'whenever' position.
     */
    inner class GivenGenerateWheneverGenerate(val whenever: GivenGenerateWhenever, subBlocks: List<ScriptBlock>) :
        CompositeScriptBlock(subBlocks) {

        fun verify(body: QueryBuilder<QF, E>.() -> Unit) = GivenGenerateWheneverGenerateVerify(this,
            QueryBuilder(verifyQueryFactory, environment).apply(body).scriptFragments)

        private val environment = whenever.given.environment
    }

    inner class GivenGenerateWheneverGenerateVerify(
        val wheneverGenerate: GivenGenerateWheneverGenerate, scriptFragments: List<String>
    ) : BasicScriptBlock("verify", scriptFragments) {

        val whenever = wheneverGenerate.whenever
        val givenGenerate = whenever.givenGenerate
        val given = givenGenerate.given

        /**
         * Constructs an oracle scenario script with the given, when, verify, and generate blocks previously constructed.
         */
        fun oracleScenario() = OracleScenarioScript(given, givenGenerate, whenever, wheneverGenerate, verify = this)
    }

}
