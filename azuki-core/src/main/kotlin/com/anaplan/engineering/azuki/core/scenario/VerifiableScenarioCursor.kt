package com.anaplan.engineering.azuki.core.scenario

import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory

class VerifiableScenarioCursor<AF : ActionFactory, CF : CheckFactory, S : VerifiableSystem<AF, CF>, SF : VerifiableSystemFactory<AF, CF, *, *, *, S>>(
    private val systemFactory: SF,
    scenario: VerifiableScenario<AF, CF>,
) {
    private val systemCursor: VerifiableSystemCursor<AF, CF, S, SF>?

    var state = State.Ok
        private set

    init {
        val declarations = scenario.declarations(systemFactory.actionFactory)
        systemCursor = if (declarations.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            Log.warn("Unsupported declaration found! Declarations: ${declarations.joinToString("\n") { "\t - $it" }}")
            state = State.UnsupportedDeclaration
            null
        } else {
            VerifiableSystemCursor(systemFactory, SystemDefinition(declarations))
        }
    }

    enum class State {
        Ok,
        UnsupportedDeclaration,
        UnsupportedCommand,
        NoChecks,
    }

    private val scenarioIterations = scenario.iterations().toMutableList()

    val isEmpty get() = scenarioIterations.isEmpty()

    fun hasNext(): Boolean {
        if (state != State.Ok || isEmpty) {
            return false
        }
        val lookAhead = scenarioIterations.first()
        if (UnsupportedAction in lookAhead.commands(systemFactory.actionFactory)) {
            state = State.UnsupportedCommand
            return false
        }
        if (lookAhead.checks(systemFactory.checkFactory).all { it == UnsupportedCheck }) {
            state = State.NoChecks
            return false
        }
        return true
    }

    fun next(): S {
        if (!hasNext()) {
            throw IllegalStateException("Cursor cannot be moved")
        }
        val scenarioIteration = scenarioIterations.removeFirst()
        val systemIteration = SystemIteration(
            commands = scenarioIteration.commands(systemFactory.actionFactory),
            checks = scenarioIteration.checks(systemFactory.checkFactory).filter { it !is UnsupportedCheck },
            regardlessOfActions = scenarioIteration.regardlessOfActions(systemFactory.actionFactory)
        )
        return systemCursor!!.apply(systemIteration)
    }

    fun destroy() = systemCursor?.destroy()

    companion object {
        private val Log = LoggerFactory.getLogger(VerifiableScenarioCursor::class.java)
    }
}

private class VerifiableSystemCursor<AF : ActionFactory, CF : CheckFactory, S : VerifiableSystem<AF, CF>, SF : VerifiableSystemFactory<AF, CF, *, *, *, S>>(
    private val systemFactory: SF,
    private var systemDefinition: SystemDefinition
) {

    val system = systemFactory.create(systemDefinition)

    fun apply(iteration: SystemIteration) =
        if (system is MutableSystem<*, *>) {
            system.applyIteration(iteration)
            system
        } else {
            systemDefinition = systemDefinition.copy(
                commands = systemDefinition.commands + iteration.commands,
                checks = iteration.checks,
                regardlessOfActions = iteration.regardlessOfActions
            )
            systemFactory.create(systemDefinition)
        }

    fun destroy() {
        if (system is MutableSystem<*, *>) {
            system.destroy()
        }
    }
}
