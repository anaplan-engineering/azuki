package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

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
        testInstance.runTask(TaskType.CreateActionGenerators, scenario) { implementation ->
            val systemFactory = implementation.createSystemFactory()
            scenario.actionGenerations(systemFactory.actionGeneratorFactory)
        }.result!!
    }


    fun run(): Result<AF, CF, QF, AGF> {
        var remainingOracles = oracleInstances
        val oracleResults = mutableListOf<OracleResult<AF, CF, QF, AGF>>()
        while (oracleResults.lastOrNull()?.verified != true && remainingOracles.isNotEmpty()) {
            val oracle = remainingOracles.first()
            remainingOracles = remainingOracles.drop(1)
            Log.info("Attempting to verify scenario, oracle=$oracle")
            val oracleResult = verifyWithOracle(oracle)
            Log.info("Verification result, oracle=$oracle verified=${oracleResult.verified}")
            oracleResults.add(oracleResult)
        }
        return Result(testInstance, oracleResults)
    }

    private fun verifyWithOracle(oracleInstance: ImplementationInstance<AF, CF, QF, AGF>): OracleResult<AF, CF, QF, AGF> {
        val resultBuilder = OracleResult.Builder(oracleInstance)
        val declarationValidTaskResult = isDeclarationValid(oracleInstance)
        resultBuilder.add(declarationValidTaskResult)
        if (declarationValidTaskResult.result != true) {
            return resultBuilder.build()
        }
        val buildActionsValidTaskResult = areBuildActionsValid(oracleInstance)
        resultBuilder.add(buildActionsValidTaskResult)
        if (buildActionsValidTaskResult.result != true) {
            return resultBuilder.build()
        }
        // TODO - must retain generate of test system -- or we get different scenario!
        var generatedScenario: OracleScenario<AF, QF, AGF> = scenario
        resultBuilder.addAll(actionGenerators.map { generators ->
            val generateTaskResult = generateActions(testInstance, generatedScenario, generators)
            val actionCreators = generateTaskResult.result ?: emptyList()
            generatedScenario = GeneratedScenario(generatedScenario, actionCreators)
            generateTaskResult
        })
        val queryTaskResult = query(testInstance, generatedScenario)
        resultBuilder.add(queryTaskResult)
        val answerCount = queryTaskResult.result?.size ?: 0
        Log.debug("Query result, oracle=$oracleInstance answerCount=$answerCount")
        if (answerCount == 0) {
            return resultBuilder.build()
        }
        val verifyTaskResult = answersSufficient(testInstance, generatedScenario, queryTaskResult.result!!)
        resultBuilder.add(verifyTaskResult)
        return resultBuilder.build(verifyTaskResult.result == true)
    }


    private fun isDeclarationValid(instance: ImplementationInstance<AF, CF, QF, AGF>) =
        instance.runTask(TaskType.CheckDeclarations, scenario) { implementation ->
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
        instance.runTask(TaskType.CheckActions, scenario) { implementation ->
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
        instance.runTask(TaskType.GenerateActions, scenario) { implementation ->
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
        instance.runTask(TaskType.Query, scenario) { implementation ->
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
        instance.runTask(TaskType.Verify, scenario) { implementation ->
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
        CF : CheckFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory
        >(
        val verified: Boolean,
        val start: LocalDateTime,
        val instance: ImplementationInstance<AF, CF, QF, AGF>,
        val taskResults: List<TaskResult<OracleScenario<AF, QF, AGF>, *>>
    ) {
        class Builder<
            AF : ActionFactory,
            CF : CheckFactory,
            QF : QueryFactory,
            AGF : ActionGeneratorFactory
            >(
            val instance: ImplementationInstance<AF, CF, QF, AGF>,
            val start: LocalDateTime = LocalDateTime.now()
        ) {
            private val taskResults = mutableListOf <TaskResult<OracleScenario<AF, QF, AGF>, *>>()

            fun add(taskResult: TaskResult<OracleScenario<AF, QF, AGF>, *>) = taskResults.add(taskResult)

            fun addAll(taskResults: List<TaskResult<OracleScenario<AF, QF, AGF>, *>>) = this.taskResults.addAll(taskResults)

            fun build(verified: Boolean = false) = OracleResult(verified, start, instance, taskResults)
        }
    }

    class Result<
        AF : ActionFactory,
        CF : CheckFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory
        >(
        val testInstance: ImplementationInstance<AF, CF, QF, AGF>,
        val oracleResults: List<OracleResult<AF, CF, QF, AGF>>
    ) {

        val verified = oracleResults.any { it.verified }
        val verifiedBy = oracleResults.find { it.verified }?.instance

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
