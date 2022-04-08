package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory

class VerifiableScenarioRunner<S : VerifiableScenario<AF, CF>, AF : ActionFactory, CF : CheckFactory>(
    private val implementationInstance: ImplementationInstance<AF, CF, *, *>,
    private val scenario: S,
    private val runName: String,
) {

    fun run(): Result {
        val taskResult = implementationInstance.runTask(TaskType.Verify, scenario) { implementation ->
            println("Using implementation '${implementation.name}'")
            runScenario(implementation.createSystemFactory())
        }
        return taskResult.result ?: Result.UnknownError
    }

    private fun runScenario(systemFactory: SystemFactory<AF, CF, *, *, *>): Result {
        val declarations = scenario.definitions(systemFactory.actionFactory)
        if (declarations.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            Log.warn("Unsupported declaration found! Declarations: ${declarations.joinToString("\n") { "\t - $it" }}")
            return Result.UnsupportedDeclaration
        }
        val actions = scenario.buildActions(systemFactory.actionFactory)
        if (actions.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            Log.warn("Unsupported action found! Actions: ${actions.joinToString("\n") { "\t - $it" }}")
            return Result.UnsupportedAction
        }
        val checks = scenario.checks(systemFactory.checkFactory)
        if (checks.all { it is UnsupportedCheck }) {
            return Result.NoSupportedChecks
        }
        val supportedChecks = checks.filter { it !is UnsupportedCheck }
        val regardlessOfActions = scenario.regardlessOfActions(systemFactory.actionFactory)
        return verifySystem(buildSystem(systemFactory, declarations, actions, supportedChecks, regardlessOfActions))
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

    private fun verifySystem(system: System<AF, CF>) =
        try {
            if (System.SystemAction.Verify in system.supportedActions) {
                val verificationResult = system.verify()
                when (verificationResult) {
                    is VerificationResult.Verified -> Result.Verified
                    is VerificationResult.Unverified -> Result.Unverified
                    is VerificationResult.SystemInvalid -> throw verificationResult.cause
                }
            } else if (System.SystemAction.Report in system.supportedActions) {
                system.generateReport(runName)
                Result.Reported
            } else {
                Result.IncompatibleSystem
            }

        } catch (e: LateDetectUnsupportedActionException) {
            Result.UnsupportedAction
        } catch (e: LateDetectUnsupportedCheckException) {
            Result.UnsupportedCheck
        }

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
        UnknownError
    }

}
