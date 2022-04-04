package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory

class MultiOracleScenarioRunner<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    >(
    private val testInstance: ImplementationInstance<AF, CF, QF, AGF>,
    private val oracleInstances: List<ImplementationInstance<AF, CF, QF, AGF>>,
    private val scenario: OracleScenario<AF, QF, AGF>,
    private val runName: String,
) {

    init {
        if (oracleInstances.isEmpty()) {
            throw IllegalArgumentException("Must have at least one oracle instance to verify against")
        }
    }

    private val actionGenerators by lazy {
        testInstance.runTask("create-action-generators", scenario) { implementation ->
            val systemFactory = implementation.createSystemFactory()
            scenario.actionGenerations(systemFactory.actionGeneratorFactory)
        }.result!!
    }


    fun run(): Result<AF, QF, AGF> {
        var remainingOracles = oracleInstances
        val oracleResults = mutableListOf<OracleResult<AF, QF, AGF>>()
        while (oracleResults.lastOrNull()?.verified != true && remainingOracles.isNotEmpty()) {
            val oracle = remainingOracles.first()
            remainingOracles = remainingOracles.drop(1)
            Log.info("Attempting to verify scenario, oracle=$oracle")
            val oracleResult = verifyWithOracle(oracle)
            Log.info("Verification result, oracle=$oracle verified=${oracleResult.verified}")
            oracleResults.add(oracleResult)
        }
        return Result(oracleResults)
    }

    private fun verifyWithOracle(oracleInstance: ImplementationInstance<AF, CF, QF, AGF>): OracleResult<AF, QF, AGF> {
        val declarationValidTaskResult = isDeclarationValid(oracleInstance)
        if (declarationValidTaskResult.result != true) {
            return OracleResult(false, oracleInstance.instanceName, listOf(declarationValidTaskResult))
        }
        val buildActionsValidTaskResult = areBuildActionsValid(oracleInstance)
        if (buildActionsValidTaskResult.result != true) {
            return OracleResult(false,
                oracleInstance.instanceName,
                listOf(declarationValidTaskResult, buildActionsValidTaskResult))
        }
        // TODO - c/should we cache generate/query of test system
        var generatedScenario: OracleScenario<AF, QF, AGF> = scenario
        val generateTaskResults = actionGenerators.map { generators ->
            val generateTaskResult = generateActions(testInstance, generatedScenario, generators)
            val actionCreators = generateTaskResult.result ?: emptyList()
            generatedScenario = GeneratedScenario(generatedScenario, actionCreators)
            generateTaskResult
        }
        val queryTaskResult = query(testInstance, generatedScenario)
        val answerCount = queryTaskResult.result?.size ?: 0
        Log.debug("Query result, oracle=$oracleInstance answerCount=$answerCount")
        if (answerCount == 0) {
            return OracleResult(false,
                oracleInstance.instanceName,
                listOf(declarationValidTaskResult, buildActionsValidTaskResult) + generateTaskResults + queryTaskResult
            )
        }
        val verifyTaskResult = answersSufficient(testInstance, generatedScenario, queryTaskResult.result!!)
        return OracleResult(
            verifyTaskResult.result == true,
            oracleInstance.instanceName,
            listOf(declarationValidTaskResult,
                buildActionsValidTaskResult) + generateTaskResults + queryTaskResult + verifyTaskResult
        )
    }


    private fun isDeclarationValid(instance: ImplementationInstance<AF, CF, QF, AGF>) =
        instance.runTask("check-declaration", scenario) { implementation ->
            val systemFactory = implementation.createSystemFactory()
            val declarations = scenario.definitions(systemFactory.actionFactory)
            if (declarations.any { it is UnsupportedAction }) {
                false
            } else {
                val system = systemFactory.create(
                    SystemDefinition(
                        declarations = declarations,
                        actions = emptyList(),
                        checks = listOf(systemFactory.checkFactory.systemValid()),
                    )
                )
                when (system.verify()) {
                    is VerificationResult.Verified -> true
                    else -> false
                }
            }
        }

    private fun areBuildActionsValid(instance: ImplementationInstance<AF, CF, QF, AGF>) =
        instance.runTask("check-actions", scenario) { implementation ->
            val systemFactory = implementation.createSystemFactory()
            val declarations = scenario.definitions(systemFactory.actionFactory)
            val buildActions = scenario.buildActions(systemFactory.actionFactory)
            if (buildActions.isEmpty()) {
                true
            } else if (buildActions.any { it is UnsupportedAction }) {
                false
            } else {
                val system = systemFactory.create(
                    SystemDefinition(
                        declarations = declarations,
                        actions = buildActions,
                        checks = listOf(systemFactory.checkFactory.systemValid()),
                    )
                )
                when (system.verify()) {
                    is VerificationResult.Verified -> true
                    else -> false
                }
            }
        }


    private fun generateActions(
        instance: ImplementationInstance<AF, CF, QF, AGF>,
        scenario: OracleScenario<AF, QF, AGF>,
        generators: List<ActionGenerator>
    ) =
        instance.runTask("generate", scenario) { implementation ->
            val systemFactory = implementation.createSystemFactory()
            val declarations = scenario.definitions(systemFactory.actionFactory)
            if (UnsupportedActionGenerator in generators) {
                Log.warn("Unsupported action generator found!!")
                emptyList()
            } else try {
                val system = systemFactory.create(
                    SystemDefinition(
                        declarations = declarations,
                        actions = emptyList(),
                        actionGenerators = generators,
                    )
                )
                system.generateActions()
            } catch (e: LateDetectUnsupportedActionException) {
                emptyList()
            }
        }

    private fun query(instance: ImplementationInstance<AF, CF, QF, AGF>, scenario: OracleScenario<AF, QF, AGF>) =
        instance.runTask("query", scenario) { implementation ->
            val systemFactory = implementation.createSystemFactory()
            val declarations = scenario.definitions(systemFactory.actionFactory)
            val buildActions = scenario.buildActions(systemFactory.actionFactory)
            if (UnsupportedAction in declarations || UnsupportedAction in buildActions) {
                emptyList()
            } else try {
                val queries = scenario.queries(systemFactory.queryFactory)
                val system = systemFactory.create(
                    SystemDefinition(
                        declarations = declarations,
                        actions = buildActions,
                        queries = queries.queries.filter { it !is UnsupportedQuery<*> },
                        forAllQueries = queries.forAllQueries,
                    )
                )
                system.query()
            } catch (e: LateDetectUnsupportedActionException) {
                emptyList()
            }
        }

    private fun answersSufficient(
        instance: ImplementationInstance<AF, CF, QF, AGF>,
        scenario: OracleScenario<AF, QF, AGF>,
        answers: List<Answer<*, CF>>
    ) =
        instance.runTask("verify", scenario) { implementation ->
            val systemFactory = implementation.createSystemFactory()
            val declarations = scenario.definitions(systemFactory.actionFactory)
            val buildActions = scenario.buildActions(systemFactory.actionFactory)
            val system = systemFactory.create(
                SystemDefinition(
                    declarations = declarations,
                    actions = buildActions,
                    checks = answers.flatMap { it.createChecks(systemFactory.checkFactory) },
                )
            )
            when (system.verify()) {
                is VerificationResult.Verified -> true
                else -> false
            }
        }

    companion object {
        private val Log = LoggerFactory.getLogger(MultiOracleScenarioRunner::class.java)
    }


    class OracleResult<
        AF : ActionFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory
        >(
        val verified: Boolean,
        val instanceName: String,
        val taskResults: List<TaskResult<OracleScenario<AF, QF, AGF>, *>>
    )

    class Result<
        AF : ActionFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory
        >(
        val oracleResults: List<OracleResult<AF, QF, AGF>>
    ) {
        val verified = oracleResults.any { it.verified }
        val verifiedBy = oracleResults.find { it.verified }?.instanceName

        override fun toString() =
            if (verified) {
                "Scenario verifed by $verifiedBy"
            } else {
                "Scenario not verified"
            }
    }

    private class GeneratedScenario<
        AF : ActionFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory
        >(
        private val base: OracleScenario<AF, QF, AGF>,
        private val actionCreators: List<(AF) -> Action>
    ) : OracleScenario<AF, QF, AGF> {

        override fun definitions(actionFactory: AF) =
            base.definitions(actionFactory) + actionCreators.map { it(actionFactory) }


        override fun buildActions(actionFactory: AF) = base.buildActions(actionFactory)

        override fun queries(queryFactory: QF) = base.queries(queryFactory)

        override fun actionGenerations(actionGeneratorFactory: AGF) =
            throw UnsupportedOperationException()
    }

}
