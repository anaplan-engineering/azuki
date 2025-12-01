package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.*

/**
 * The main endpoint for script generation tasks.
 */
class ScriptGenerationService<
    out AF : ActionFactory,
    out CF : CheckFactory,
    out QF : QueryFactory,
    out AGF : ActionGeneratorFactory,
    DS : DeclarationState,
    E : ScriptGenerationEnvironment,
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
     * Incremental builder for script generators.
     */
    class Builder<
        // mandatory factories
        out AF : ActionFactory,
        out CF : CheckFactory,
        // optional factories (these require calling 'withXYZFactory')
        out QF : QueryFactory,
        out AGF : ActionGeneratorFactory,
        // mandatory non-factory parameters
        DS : DeclarationState,
        // optional non-factory parameters (these require calling 'withXYZ')
        E : ScriptGenerationEnvironment,
        >(
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

        fun build() = ScriptGenerationService(
            actionFactory,
            checkFactory,
            declarationStateFactory,
            environmentFactory,
            queryQueryFactory,
            verifyQueryFactory,
            actionGeneratorFactory,
        )

        /**
         * Adds an environment factory to this service, changing the type of accepted scenarios accordingly.
         */
        fun <N : ScriptGenerationEnvironment> withEnvironmentFactory(new: ScriptGenerationEnvironmentFactory<N>) =
            Builder(
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
        fun <N : QueryFactory> withQueryFactories(query: N, verify: N) = Builder(
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
        fun <N : ActionGeneratorFactory> withActionGeneratorFactory(new: N) = Builder(
            actionFactory,
            checkFactory,
            declarationStateFactory,
            environmentFactory,
            queryQueryFactory,
            verifyQueryFactory,
            actionGeneratorFactory = new,
        )
    }

    /**
     * Provides ready-made usage patterns for script generation of a whole scenario, from a whole scenario.
     */
    val scenario = ScriptGenerationScenarioPatterns(this)


    /**
     * Constructs a given block by mixing in declarations from one or more sources.
     */
    fun given(body: GivenBuilder<AF, DS, E>.() -> Unit) = environmentFactory.create().let { e ->
        Given(e, GivenBuilder(actionFactory, declarationStateFactory, e).build(body))
    }

    companion object {

        /**
         * Starts building a basic script generation service.
         *
         * If your implementation of Azuki uses a script generation environment, or supports queries and/or action
         * generators, you will need to call additional methods on the result of this method to pass them in.
         */
        fun <AF : ActionFactory, CF : CheckFactory, DS : DeclarationState> new(
            actionFactory: AF, checkFactory: CF, declarationStateFactory: DeclarationStateFactory<DS>
        ) = Builder(
            actionFactory,
            checkFactory,
            declarationStateFactory,
            environmentFactory = { NoScriptGenerationEnvironment },
            NoQueryFactory,
            NoQueryFactory,
            NoActionGeneratorFactory,
        )

        /**
         * A basic service that is independent of any Azuki adapter.
         *
         * This is useful when generating scripts directly from string script fragments.  In all other cases,
         * create a `ScriptGenerationService` to be able to generate scripts from scenarios and system definitions.
         */
        val standalone by lazy {
            new(
                object : ActionFactory {},
                object : CheckFactory {},
                { object : DeclarationState() {} },
            ).build()
        }
    }

    inner class Given(internal val environment: E, contents: List<ScriptElement>) {

        val block = ScriptBlock("given", contents)

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

    abstract inner class Whenever(contents: List<ScriptElement>) {

        val block = ScriptBlock("whenever", contents)
        abstract val given: Given
        protected val environment get() = given.environment
    }

    /**
     * A whenever block following a given block.
     */
    inner class GivenWhenever(override val given: Given, contents: List<ScriptElement>) : Whenever(contents) {

        /**
         * Finishes generation of an incomplete verifiable scenario.
         */
        fun incompleteScenario() = IncompleteScenarioScript(given.block, whenever = block)

        /**
         * Constructs a then block by mixing in checks from one or more sources.
         */
        fun then(body: ThenBuilder<CF, E>.() -> Unit) =
            GivenWheneverThen(this, ThenBuilder(checkFactory, environment).build(body))

        /**
         * Constructs a query block by mixing in queries from one or more sources.
         */
        fun query(body: QueryBuilder<QF, E>.() -> Unit) =
            GivenWheneverQuery(this, QueryBuilder(queryQueryFactory, environment).build(body))
    }

    inner class GivenWheneverThen(val whenever: GivenWhenever, contents: List<ScriptElement>) {

        val block = ScriptBlock("then", contents)
        internal val given get() = whenever.given

        /**
         * Constructs a verifiable scenario script with the given, when, and then blocks previously constructed.
         */
        fun verifiableScenario() = VerifiableScenarioScript(given.block, whenever.block, block)
    }

    inner class GivenWheneverQuery(val whenever: GivenWhenever, contents: List<ScriptElement>) {

        val block = ScriptBlock("query", contents)
        val given = whenever.given

        /**
         * Constructs a query scenario script with the given, when, and query blocks previously constructed.
         */
        fun queryScenario() = QueryScenarioScript(given.block, whenever.block, block)
    }

    /**
     * A list of generate blocks in 'given' position.
     */
    inner class GivenGenerate(val given: Given, subBlocks: List<ScriptBlock>) {

        val blockList = ScriptElementList<ScriptBlock>(subBlocks)

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
    inner class GivenGenerateWhenever(val givenGenerate: GivenGenerate, contents: List<ScriptElement>) :
        Whenever(contents) {

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
    inner class GivenGenerateWheneverGenerate(val whenever: GivenGenerateWhenever, subBlocks: List<ScriptBlock>) {

        val blockList = ScriptElementList<ScriptBlock>(subBlocks)
        val givenGenerate = whenever.givenGenerate
        val given = whenever.given

        /**
         * Constructs a verify block by mixing in queries from one or more sources.
         */
        fun verify(body: QueryBuilder<QF, E>.() -> Unit) =
            GivenGenerateWheneverGenerateVerify(this, QueryBuilder(verifyQueryFactory, environment).build(body))

        private val environment = whenever.given.environment
    }

    /**
     * A verify block following a whenever-generate block.
     */
    inner class GivenGenerateWheneverGenerateVerify(
        val wheneverGenerate: GivenGenerateWheneverGenerate, contents: List<ScriptElement>
    ) {

        val block = ScriptBlock("verify", contents)
        val givenGenerate = wheneverGenerate.givenGenerate
        val given = wheneverGenerate.given
        val whenever = wheneverGenerate.whenever

        // We need the `whenever` block to appear to make sure the `generate` block after it is considered a
        // whenever-generate block, but it's empty, so is liable to be omitted by the renderer.  Solve this by
        // replacing it with a `whenever` block that always claims to be non-empty.
        private val oracleWheneverBlock by lazy {
            val existing = wheneverGenerate.whenever.block
            val mustGenerateWhenever = existing.isEmpty && !wheneverGenerate.blockList.isEmpty
            if (mustGenerateWhenever) existing.asNonEmpty else existing
        }

        /**
         * Constructs an oracle scenario script with the given, when, verify, and generate blocks previously constructed.
         */
        fun oracleScenario() = OracleScenarioScript(given.block,
            givenGenerate.blockList,
            oracleWheneverBlock,
            wheneverGenerate.blockList,
            this.block)

        /**
         * Use the given and whenever blocks from this oracle scenario to start building towards a verifiable scenario.
         */
        fun takeGivenAndWhenever() = GivenWhenever(given, whenever.block.elements)
    }
}
