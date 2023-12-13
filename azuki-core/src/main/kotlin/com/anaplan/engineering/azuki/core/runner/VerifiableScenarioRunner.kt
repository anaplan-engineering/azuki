package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory
import java.io.File

class VerifiableScenarioRunner<S : VerifiableScenario<AF, CF>, AF : ActionFactory, CF : CheckFactory>(
    private val implementationInstance: ImplementationInstance<AF, CF, *, *>,
    private val persistenceVerificationInstance: ImplementationInstance<AF, CF, *, *>?,
    private val scenario: S,
    private val runName: String,
) {

    fun run(): Result {
        val taskResult = implementationInstance.runTask(TaskType.Verify, scenario) { implementation ->
            Log.info("Using implementation '${implementation.name}'")
            runScenario(implementation.createSystemFactory())
        }
        return if (taskResult.result?.result == Result.Verified && persistenceVerificationInstance != null) {
            val pvTaskResult =
                persistenceVerificationInstance.runTask(TaskType.PersistenceVerify, scenario) { implementation ->
                    Log.info("Using persistence verification implementation '${implementation.name}'")
                    verifyPersistence(implementation.createSystemFactory(), taskResult.result.persistenceContext!!)
                }
            pvTaskResult.result ?: Result.UnknownError
        } else {
            taskResult.result?.result ?: Result.UnknownError
        }
    }

    private data class RunScenarioResult(
        val result: Result,
        val persistenceContext: File? = null
    )

    private fun runScenario(systemFactory: SystemFactory<AF, CF, *, *, *>): RunScenarioResult {
        val declarations = scenario.definitions(systemFactory.actionFactory)
        if (declarations.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            Log.warn("Unsupported declaration found! Declarations: ${declarations.joinToString("\n") { "\t - $it" }}")
            return RunScenarioResult(Result.UnsupportedDeclaration)
        }
        val actions = scenario.buildActions(systemFactory.actionFactory)
        if (actions.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            Log.warn("Unsupported action found! Actions: ${actions.joinToString("\n") { "\t - $it" }}")
            return RunScenarioResult(Result.UnsupportedAction)
        }
        val checks = scenario.checks(systemFactory.checkFactory)
        if (checks.all { it is UnsupportedCheck }) {
            return RunScenarioResult(Result.NoSupportedChecks)
        }
        val supportedChecks = checks.filter { it !is UnsupportedCheck }
        val regardlessOfActions = scenario.regardlessOfActions(systemFactory.actionFactory)
        val system = buildSystem(systemFactory, declarations, actions, supportedChecks, regardlessOfActions)
        return verifySystem(system)
    }

    private fun verifySystem(system: System<AF, CF>) =
        try {
            if (persistenceVerificationInstance != null) {
                if (system !is PersistableSystem) {
                    Log.warn("Persistence verification instance specified, but implementation system is not persistable")
                    RunScenarioResult(Result.NotPersistable)
                } else {
                    verifyWithSerialize(system)
                }
            } else {
                verifyWithoutSerialize(system)
            }
        } catch (e: LateDetectUnsupportedActionException) {
            RunScenarioResult(Result.UnsupportedAction)
        } catch (e: LateDetectUnsupportedCheckException) {
            RunScenarioResult(Result.UnsupportedCheck)
        }

    private fun verifyWithoutSerialize(system: System<AF, CF>) =
        RunScenarioResult(
        if (System.SystemAction.Verify in system.supportedActions) {
            when (val verificationResult = system.verify()) {
                is VerificationResult.Verified, is VerificationResult.VerifiedAndSerialized -> Result.Verified
                is VerificationResult.Unverified -> Result.Unverified
                is VerificationResult.SystemInvalid -> throw verificationResult.cause
            }
        } else if (System.SystemAction.Report in system.supportedActions) {
            system.generateReport(runName)
            Result.Reported
        } else {
            Result.IncompatibleSystem
        }
    )

    private fun verifyWithSerialize(system: System<AF, CF>) =
        when (val verificationResult = (system as PersistableSystem).verifyAndSerialize()) {
            is VerificationResult.VerifiedAndSerialized ->
                RunScenarioResult(Result.Verified, verificationResult.file)

            is VerificationResult.Verified -> RunScenarioResult(Result.NotPersistable)
            is VerificationResult.Unverified -> RunScenarioResult(Result.Unverified)
            is VerificationResult.SystemInvalid -> throw verificationResult.cause
        }

    private fun verifyPersistence(systemFactory: SystemFactory<AF, CF, *, *, *>, persistenceContext: File): Result {
        val checks = scenario.checks(systemFactory.checkFactory)
        if (checks.all { it is UnsupportedCheck }) {
            return Result.NoSupportedChecks
        }
        val supportedChecks = checks.filter { it !is UnsupportedCheck }
        val regardlessOfActions = scenario.regardlessOfActions(systemFactory.actionFactory)
        val system = buildSystem(systemFactory, emptyList(), emptyList(), supportedChecks, regardlessOfActions)
        if (system !is PersistableSystem) {
            Log.warn("Persistence verification instance specified, but persistence verification system is not persistable")
            return Result.NotPersistable
        }
        return try {
            val verificationResult = (system as PersistableSystem).deserializeAndVerify(persistenceContext)
            when (verificationResult) {
                is VerificationResult.Verified -> Result.Verified
                is VerificationResult.Unverified -> Result.Unverified
                is VerificationResult.SystemInvalid -> throw verificationResult.cause
                else -> throw IllegalStateException("Unexpected verification result: $verificationResult")
            }
        } catch (e: LateDetectUnsupportedActionException) {
            Result.UnsupportedAction
        } catch (e: LateDetectUnsupportedCheckException) {
            Result.UnsupportedCheck
        }
    }

    private fun buildSystem(
        systemFactory: SystemFactory<AF, CF, *, *, *>,
        declarations: List<Action>,
        actions: List<Action>,
        checks: List<Check>,
        regardlessOfActions: List<List<Action>>
    ) = systemFactory.create(
        SystemDefinition(
            declarations = declarations,
            actions = actions,
            checks = checks,
            regardlessOfActions = regardlessOfActions
        )
    )

    companion object {
        private val Log = LoggerFactory.getLogger(VerifiableScenarioRunner::class.java)
    }

    enum class Result {
        Verified,
        Unverified,
        Reported,
        UnsupportedDeclaration,
        UnsupportedAction,
        UnsupportedCheck,
        NoSupportedChecks,
        IncompatibleSystem,
        UnknownError,
        NotPersistable
    }

}
