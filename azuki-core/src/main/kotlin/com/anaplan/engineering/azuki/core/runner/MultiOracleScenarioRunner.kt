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

    fun run(): Result<AF, CF, QF, AGF> {
        var remainingOracles = oracleInstances
        val oracleResults = mutableListOf<OracleResult<AF, CF, QF, AGF>>()
        var verificationContext: VerificationContext<AF, CF, QF, AGF>? = null
        while (oracleResults.lastOrNull()?.verified != true && remainingOracles.isNotEmpty()) {
            val oracle = remainingOracles.first()
            val resultBuilder = OracleResult.Builder(oracle)
            verificationContext = establishVerificationContext(oracle, resultBuilder, verificationContext)
            val oracleResult = try {
                if (verificationContext.answers == null) {
                    resultBuilder.build()
                } else {
                    Log.info("Attempting to verify scenario, oracle=$oracle")
                    verifyWithOracle(oracle, resultBuilder, verificationContext)
                }
            } catch (t: Throwable) {
                Log.error("Unexpected error when verifying, oracle=$oracle", t)
                OracleResult(false, LocalDateTime.now(), oracle, emptyList())
            }
            Log.info("Verification result, oracle=$oracle verified=${oracleResult.verified}")
            oracleResults.add(oracleResult)
            remainingOracles = remainingOracles.drop(1)
        }
        return Result(testInstance, verificationContext?.scenario, oracleResults)
    }

    private class VerificationContext<
        AF : ActionFactory,
        CF : CheckFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory,
        >(
        val answers: List<Answer<*, CF>>?, val scenario: OracleScenario<AF, QF, AGF>
    )

    private fun establishVerificationContext(
        oracleInstance: ImplementationInstance<AF, CF, QF, AGF>,
        resultBuilder: OracleResult.Builder<AF, CF, QF, AGF>,
        verificationContext: VerificationContext<AF, CF, QF, AGF>?,
    ): VerificationContext<AF, CF, QF, AGF> {
        if (verificationContext?.answers != null) {
            // if we have an existing verification context we can assume scenario is valid and we want to ensure
            // that we use the same generated actions and answers for all oracles
            return verificationContext
        }
        val generatedScenario = generateScenario(resultBuilder)
        if (resultBuilder.error != null) {
            Log.error("Unable to generate scenario due to previous error", resultBuilder.error)
            return VerificationContext(null, generatedScenario)
        }
        val declarationValidTaskResult = isDeclarationValid(oracleInstance, generatedScenario)
        resultBuilder.add(declarationValidTaskResult)
        if (declarationValidTaskResult.result != true) {
            return VerificationContext(null, generatedScenario)
        }
        val commandsValidTaskResult = areCommandsValid(oracleInstance, generatedScenario)
        resultBuilder.add(commandsValidTaskResult)
        if (commandsValidTaskResult.result != true) {
            return VerificationContext(null, generatedScenario)
        }
        val queryTaskResult = query(testInstance, generatedScenario)
        resultBuilder.add(queryTaskResult)
        val answerCount = queryTaskResult.result?.size ?: 0
        Log.debug("Query result, oracle={} answerCount={}", testInstance, answerCount)
        if (answerCount == 0) {
            return VerificationContext(null, generatedScenario)
        }
        return VerificationContext(queryTaskResult.result, generatedScenario)
    }

    private enum class ActionGeneratorType { Given, When }

    private fun verifyWithOracle(
        oracleInstance: ImplementationInstance<AF, CF, QF, AGF>,
        resultBuilder: OracleResult.Builder<AF, CF, QF, AGF>,
        verificationContext: VerificationContext<AF, CF, QF, AGF>,
    ): OracleResult<AF, CF, QF, AGF> {
        val verifyTaskResult =
            answersSufficient(oracleInstance, verificationContext.scenario, verificationContext.answers!!)
        resultBuilder.add(verifyTaskResult)
        return resultBuilder.build(verifyTaskResult.result == true)
    }

    private fun isDeclarationValid(
        instance: ImplementationInstance<AF, CF, QF, AGF>,
        scenario: OracleScenario<AF, QF, AGF>,
    ) = instance.runTask(TaskType.CheckDeclarations, scenario) { implementation ->
        val systemFactory =
            implementation.createSystemFactory() as? VerifiableSystemFactory<AF, CF, QF, AGF, *, VerifiableSystem<AF, CF>>
                ?: throw IllegalStateException("Trying to verify, but system factory does not create verifiable systems")
        val declarations = scenario.declarations(systemFactory.actionFactory)
        val result = if (declarations.any { it is UnsupportedAction }) {
            Log.debug("Declarations contain unsupported action")
            false
        } else {
            val system = systemFactory.create(SystemDefinition(
                declarations = declarations,
                commands = emptyList(),
                checks = listOf(systemFactory.checkFactory.systemValid()),
            ))
            try {
                when (system.verify()) {
                    is VerificationResult.Verified -> true
                    else -> false
                }
            } finally {
                if (system is MutableSystem<*, *>) system.destroy()
            }
        }
        Log.debug("Checked declarations implementation=${implementation.name} result=$result")
        result
    }

    private fun areCommandsValid(
        instance: ImplementationInstance<AF, CF, QF, AGF>,
        scenario: OracleScenario<AF, QF, AGF>,
    ) = instance.runTask(TaskType.CheckActions, scenario) { implementation ->
        val systemFactory =
            implementation.createSystemFactory() as? VerifiableSystemFactory<AF, CF, QF, AGF, *, VerifiableSystem<AF, CF>>
                ?: throw IllegalStateException("Trying to validate, but system factory does not create verifiable systems")
        val declarations = scenario.declarations(systemFactory.actionFactory)
        val commands = scenario.commands(systemFactory.actionFactory)
        if (commands.isEmpty()) {
            true
        } else if (commands.any { it is UnsupportedAction }) {
            false
        } else {
            val system = systemFactory.create(SystemDefinition(
                declarations = declarations,
                commands = commands,
                checks = listOf(systemFactory.checkFactory.systemValid()),
            ))
            try {
                when (val result = system.verify()) {
                    is VerificationResult.Verified -> true
                    is VerificationResult.SystemInvalid -> {
                        Log.error("System invalid:", result.cause)
                        false
                    }

                    else -> false
                }
            } finally {
                if (system is MutableSystem<*, *>) system.destroy()
            }
        }
    }

    private class ActionGeneratingSystemCursor<AF : ActionFactory, CF : CheckFactory, S : ActionGeneratingSystem<AF, CF>, SF : ActionGeneratingSystemFactory<AF, CF, *, *, *, S>>(
        private val systemFactory: SF,
        private var systemDefinition: SystemDefinition
    ) {

        private val system = systemFactory.create(systemDefinition)

        fun apply(iteration: SystemIteration) =
            if (system is MutableSystem<*, *>) {
                system.applyIteration(iteration)
                system
            } else {
                systemDefinition = systemDefinition.copy(
                    commands = systemDefinition.commands + iteration.commands,
                    actionGenerators = iteration.actionGenerators,
                )
                systemFactory.create(systemDefinition)
            }

        fun destroy() {
            if (system is MutableSystem<*, *>) {
                system.destroy()
            }
        }
    }

    private fun generateScenario(resultBuilder: OracleResult.Builder<AF, CF, QF, AGF>): OracleScenario<AF, QF, AGF> {
        val actionGenerators = testInstance.runTask(TaskType.CreateActionGenerators, scenario) { implementation ->
            val systemFactory =
                implementation.createSystemFactory() as? ActionGeneratingSystemFactory<AF, CF, QF, AGF, *, ActionGeneratingSystem<AF, CF>>
                    ?: throw IllegalStateException("Trying to generate actions, but system factory does not create systems with action generation capability")
            scenario.givenActionGenerations(systemFactory.actionGeneratorFactory).map {
                it to ActionGeneratorType.Given
            } + scenario.whenActionGenerations(systemFactory.actionGeneratorFactory).map {
                it to ActionGeneratorType.When
            }
        }.result!!
        val generateTaskResult = testInstance.runTask(TaskType.GenerateActions, scenario) { implementation ->
            val systemFactory =
                implementation.createSystemFactory() as? ActionGeneratingSystemFactory<AF, CF, QF, AGF, *, ActionGeneratingSystem<AF, CF>>
                    ?: throw IllegalStateException("Trying to generate actions, but system factory does not create systems with action generation capability")
            val declarations = scenario.declarations(systemFactory.actionFactory)
            val commands = scenario.commands(systemFactory.actionFactory)
            val systemCursor = ActionGeneratingSystemCursor(systemFactory, SystemDefinition(
                declarations = declarations,
                commands = commands
            ))
            try {
                actionGenerators.fold(scenario) { baseScenario, (generators, type) ->
                    val commandsFromPreviousIteration =
                        if (baseScenario is GeneratedScenario) {
                            baseScenario.givenActionCreators.map { it(systemFactory.actionFactory) } +
                                baseScenario.whenActionCreators.map { it(systemFactory.actionFactory) }
                        } else {
                            emptyList()
                        }
                    val actionCreators = generateActions(systemCursor, commandsFromPreviousIteration, generators)
                    GeneratedScenario(
                        base = baseScenario,
                        givenActionCreators = if (type == ActionGeneratorType.Given) actionCreators else emptyList(),
                        whenActionCreators = if (type == ActionGeneratorType.When) actionCreators else emptyList()
                    )
                }
            } finally {
                systemCursor.destroy()
            }
        }
        resultBuilder.add(generateTaskResult)
        return generateTaskResult.result ?: scenario
    }


    private fun generateActions(
        systemCursor: ActionGeneratingSystemCursor<AF, CF, ActionGeneratingSystem<AF, CF>, ActionGeneratingSystemFactory<AF, CF, QF, AGF, *, ActionGeneratingSystem<AF, CF>>>,
        commands: List<Action>,
        generators: List<ActionGenerator>
    ): List<(AF) -> Action> {
        return if (UnsupportedActionGenerator in generators) {
            Log.warn("Unsupported action generator found!!")
            emptyList()
        } else try {
            val system = systemCursor.apply(SystemIteration(commands = commands, actionGenerators = generators))
            system.generateActions()
        } catch (e: LateDetectUnsupportedActionException) {
            emptyList()
        }
    }

    private fun query(instance: ImplementationInstance<AF, CF, QF, AGF>, scenario: OracleScenario<AF, QF, AGF>) =
        instance.runTask(TaskType.Query, scenario) { implementation ->
            val systemFactory =
                implementation.createSystemFactory() as? QueryableSystemFactory<AF, CF, QF, AGF, *, QueryableSystem<AF, CF>>
                    ?: throw IllegalStateException("Trying to query, but system factory does not create queryable systems")
            val declarations = scenario.declarations(systemFactory.actionFactory)
            val commands = scenario.commands(systemFactory.actionFactory)
            if (UnsupportedAction in declarations || UnsupportedAction in commands) {
                Log.warn("Unsupported action found")
                emptyList()
            } else try {
                val queries = scenario.queries(systemFactory.queryFactory)
                if (queries.isEmpty()) {
                    Log.warn("No queries found!!")
                }
                val system = systemFactory.create(SystemDefinition(
                    declarations = declarations,
                    commands = commands,
                    queries = queries.queries.filter { it !is UnsupportedQuery<*> },
                    forAllQueries = queries.forAllQueries,
                ))
                try {
                    system.query()
                } finally {
                    if (system is MutableSystem<*, *>) system.destroy()
                }
            } catch (e: LateDetectUnsupportedActionException) {
                Log.warn("Late detected unsupported action found")
                emptyList()
            }
        }

    private fun answersSufficient(
        instance: ImplementationInstance<AF, CF, QF, AGF>,
        scenario: OracleScenario<AF, QF, AGF>,
        answers: List<Answer<*, CF>>
    ) = instance.runTask(TaskType.Verify, scenario) { implementation ->
        val systemFactory =
            implementation.createSystemFactory() as? VerifiableSystemFactory<AF, CF, QF, AGF, *, VerifiableSystem<AF, CF>>
                ?: throw IllegalStateException("Trying to verify, but system factory does not create verifiable systems")
        val declarations = scenario.declarations(systemFactory.actionFactory)
        val commands = scenario.commands(systemFactory.actionFactory)
        val system = systemFactory.create(SystemDefinition(
            declarations = declarations,
            commands = commands,
            checks = answers.flatMap { it.createChecks(systemFactory.checkFactory) },
        ))
        try {
            when (system.verify()) {
                is VerificationResult.Verified -> true
                else -> false
            }
        } finally {
            if (system is MutableSystem<*, *>) system.destroy()
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(MultiOracleScenarioRunner::class.java)
    }


    class OracleResult<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory>(
        val verified: Boolean,
        val start: LocalDateTime,
        val instance: ImplementationInstance<AF, CF, QF, AGF>,
        val taskResults: List<TaskResult<OracleScenario<AF, QF, AGF>, *>>
    ) {
        class Builder<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory>(
            val instance: ImplementationInstance<AF, CF, QF, AGF>, val start: LocalDateTime = LocalDateTime.now()
        ) {
            private val taskResults = mutableListOf<TaskResult<OracleScenario<AF, QF, AGF>, *>>()

            fun add(taskResult: TaskResult<OracleScenario<AF, QF, AGF>, *>) = taskResults.add(taskResult)

            fun build(verified: Boolean = false) = OracleResult(verified, start, instance, taskResults)

            val error get() = taskResults.reversed().firstOrNull { it.exception != null }?.exception
        }
    }

    class Result<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory>(
        val testInstance: ImplementationInstance<AF, CF, QF, AGF>,
        val generatedScenario: OracleScenario<AF, QF, AGF>?,
        val oracleResults: List<OracleResult<AF, CF, QF, AGF>>
    ) {
        val verified = oracleResults.any { it.verified }
        val verifiedBy = oracleResults.find { it.verified }?.instance

        override fun toString() = if (verified) {
            "Scenario verifed by $verifiedBy"
        } else {
            "Scenario not verified"
        }
    }

    private class GeneratedScenario<AF : ActionFactory, QF : QueryFactory, AGF : ActionGeneratorFactory>(
        private val base: OracleScenario<AF, QF, AGF>,
        val givenActionCreators: List<(AF) -> Action> = emptyList(),
        val whenActionCreators: List<(AF) -> Action> = emptyList(),
    ) : OracleScenario<AF, QF, AGF> {

        override fun declarations(actionFactory: AF) =
            base.declarations(actionFactory) + givenActionCreators.map { it(actionFactory) }


        override fun commands(actionFactory: AF) =
            base.commands(actionFactory) + whenActionCreators.map { it(actionFactory) }

        override fun queries(queryFactory: QF) = base.queries(queryFactory)

        // TODO --- for completeness these could/should include unresolved generators
        override fun givenActionGenerations(actionGeneratorFactory: AGF): List<List<ActionGenerator>> = emptyList()

        // TODO --- for completeness these could/should include unresolved generators
        override fun whenActionGenerations(actionGeneratorFactory: AGF): List<List<ActionGenerator>> = emptyList()
    }

}
