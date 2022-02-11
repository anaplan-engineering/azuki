package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.dsl.*
import com.anaplan.engineering.azuki.core.scenario.AbstractVerifiableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.junit.Assume
import org.junit.runner.RunWith

@RunWith(JUnitScenarioRunner::class)
abstract class RunnableScenario<
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
    RO : RegardlessOf<AF>,
    SD : SystemDefaults,
    >(
    dslProvider: DslProvider<AF, CF, QF, AGF, G, W, T, V, Q, R, RO>,
    private val implementation: Implementation<AF, CF, QF, AGF, SD>
) : AbstractVerifiableScenario<AF, CF, G, W, T, RO>(dslProvider) {

    private val systemFactory = implementation.createSystemFactory()

    fun run(name: String, ignoreWhenUnsupported: Boolean) {
        println("Using implementation '${implementation.name}'")
        val declarations = definitions(systemFactory.actionFactory)
        val actions = buildActions(systemFactory.actionFactory)
        validateActions(declarations, actions, ignoreWhenUnsupported)
        val checks = checks(systemFactory.checkFactory)
        val supportedChecks = validateChecks(checks, ignoreWhenUnsupported)
        val regardlessOfActions = regardlessOfActions(systemFactory.actionFactory)
        try {
            run(name, declarations, actions, supportedChecks, regardlessOfActions)
        } catch (e: LateDetectUnsupportedActionException) {
            handleLateDetectedUnsupportedAction(e, ignoreWhenUnsupported)
        }
    }

    private fun handleLateDetectedUnsupportedAction(
        e: LateDetectUnsupportedActionException,
        ignoreWhenUnsupported: Boolean
    ) {
        val message = "Skipping test as late detect unsupported action found: ${e.message}"
        if (!implementation.total && ignoreWhenUnsupported) {
            println(message)
            e.printStackTrace()
            Assume.assumeTrue(false)
        } else {
            throw SkippedException(message)
        }
    }

    private fun run(name: String, declarations: List<Action>, actions: List<Action>, supportedChecks: List<Check>, regardlessOfActions: List<List<Action>>) {
        val system = systemFactory.create(
            SystemDefinition(
                declarations = declarations,
                actions = actions,
                checks = supportedChecks,
                regardlessOfActions = regardlessOfActions,
            )
        )
        if (System.SystemAction.Verify in system.supportedActions) {
            when (val result = system.verify()) {
                is VerificationResult.SystemInvalid -> throw result.cause
                is VerificationResult.Unverified -> throw AssertionError("Verification checks failed")
            }
        } else if (System.SystemAction.Report in system.supportedActions) {
            system.generateReport(name)
        } else {
            throw SkippedException("Skipping test as system does not support verify or report")
        }
    }

    private fun validateChecks(checks: List<Check>, ignoreWhenUnsupported: Boolean): List<Check> {
        val supportedChecks = checks.filterNot { it is UnsupportedCheck }
        if (implementation.total && checks != supportedChecks) {
            // if we have a total implementation we should not have unsupported checks
            throw SkippedException("Skipping test as unsupported check found")
        }
        if (supportedChecks.isEmpty()) {
            val message = "Skipping test as no supported checks found"
            if (ignoreWhenUnsupported) {
                println(message)
                Assume.assumeTrue(false)
            } else {
                throw SkippedException(message)
            }
        }
        return supportedChecks
    }

    private fun validateActions(declarations: List<Action>, actions: List<Action>, ignoreWhenUnsupported: Boolean) {
        if ((declarations + actions).filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            val message = "Skipping test as unsupported action found"
            printActions("Declarations", declarations)
            printActions("Actions", actions)
            if (!implementation.total && ignoreWhenUnsupported) {
                println(message)
                Assume.assumeTrue(false)
            } else {
                throw SkippedException(message)
            }
        }
    }

    private fun printActions(header: String, actions: List<Action>) {
        println(header)
        actions.forEach {
            println("\t- $it")
        }
    }
}

class SkippedException(msg: String) : Exception(msg)
