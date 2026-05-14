package com.anaplan.engineering.azuki.core.runner.oracle

import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory


internal class ActionGeneratingSystemCursor<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    S : ActionGeneratingSystem<AF, CF>,
    SF : ActionGeneratingSystemFactory<AF, CF, QF, AGF, *, S>
    >(
    private val systemFactory: SF,
    private val scenarioDeclarations: (AF) -> List<Action>,
    private val scenarioCommands: (AF) -> List<Action>,
    scenarioGivenActionGenerations: (AGF) -> List<List<ActionGenerator>>,
    scenarioWhenActionGenerations: (AGF) -> List<List<ActionGenerator>>,
    private val systemWriter: SystemWriter<AF, CF, QF, AGF>
) {
    // For mutable:
    private var system: S? = null
    private val unappliedCommands = mutableListOf<Action>()

    private val generatedDeclarations = mutableListOf<(AF) -> Action>()
    private val generatedCommands = mutableListOf<(AF) -> Action>()
    private var generationIndex = 0

    private val testGivenActionGenerations: MutableList<List<ActionGenerator>>
    private val testWhenActionGenerations: MutableList<List<ActionGenerator>>

    private val writerGivenActionGenerations: MutableList<List<ActionGenerator>>
    private val writerWhenActionGenerations: MutableList<List<ActionGenerator>>

    private var applyCommandsInNextGeneration: Boolean

    init {
        testGivenActionGenerations =
            scenarioGivenActionGenerations(systemFactory.actionGeneratorFactory).toMutableList()
        testWhenActionGenerations = scenarioWhenActionGenerations(systemFactory.actionGeneratorFactory).toMutableList()

        writerGivenActionGenerations =
            scenarioGivenActionGenerations(
                systemWriter.actionGeneratorFactory ?: systemFactory.actionGeneratorFactory
            ).toMutableList()
        writerWhenActionGenerations =
            scenarioWhenActionGenerations(
                systemWriter.actionGeneratorFactory ?: systemFactory.actionGeneratorFactory
            ).toMutableList()

        if (testGivenActionGenerations.size != writerGivenActionGenerations.size) {
            throw ActionGenerationException("Unexpected mismatch in given action generations")
        }
        if (testWhenActionGenerations.size != writerWhenActionGenerations.size) {
            throw ActionGenerationException("Unexpected mismatch in when action generations")
        }

        applyCommandsInNextGeneration = testGivenActionGenerations.isEmpty()
    }

    fun hasNext() = testGivenActionGenerations.isNotEmpty() || testWhenActionGenerations.isNotEmpty()

    fun next() {
        if (applyCommandsInNextGeneration) {
            applyCommandsInNextGeneration = false
            Log.debug("Applying scenario commands")
            applyScenarioCommands()
        }

        if (testGivenActionGenerations.isNotEmpty()) {
            val testGivenActionGeneration = testGivenActionGenerations.removeAt(0)
            val writerGivenActionGeneration = writerGivenActionGenerations.removeAt(0)
            writeGivenGeneration(writerGivenActionGeneration)
            Log.debug("Generating declarations: generation={} generators={}",
                generationIndex,
                testGivenActionGeneration)
            generateDeclarations(testGivenActionGeneration)
            applyCommandsInNextGeneration = testGivenActionGenerations.isEmpty()
        } else {
            val testWhenActionGeneration = testWhenActionGenerations.removeAt(0)
            val writerWhenActionGeneration = writerWhenActionGenerations.removeAt(0)
            writeWhenGeneration(writerWhenActionGeneration)
            Log.debug("Generating commands: generation={} generators={}",
                generationIndex,
                testWhenActionGeneration)
            generateCommands(testWhenActionGeneration)
        }
        generationIndex++
    }

    private fun writeGivenGeneration(actionGeneration: List<ActionGenerator>) {
        val actionFactory = systemWriter.actionFactory ?: systemFactory.actionFactory
        val systemDefinition = SystemDefinition(
            declarations = scenarioDeclarations(actionFactory) + generatedDeclarations.map { it(actionFactory) },
            actionGenerators = actionGeneration,
        )
        systemWriter.write(systemDefinition, "generateDeclarations-generation${generationIndex}")
    }

    private fun writeWhenGeneration(actionGeneration: List<ActionGenerator>) {
        val actionFactory = systemWriter.actionFactory ?: systemFactory.actionFactory
        val systemDefinition = SystemDefinition(
            declarations = scenarioDeclarations(actionFactory) + generatedDeclarations.map { it(actionFactory) },
            commands = scenarioCommands(actionFactory) + generatedCommands.map { it(actionFactory) },
            actionGenerators = actionGeneration,
        )
        systemWriter.write(systemDefinition, "generateCommands-generation${generationIndex}")
    }

    private fun generateDeclarations(actionGeneration: List<ActionGenerator>) {
        checkForUnsupported(actionGeneration)
        val actionFactory = systemFactory.actionFactory
        val systemDefinition = SystemDefinition(
            declarations = scenarioDeclarations(actionFactory) + generatedDeclarations.map { it(actionFactory) },
            actionGenerators = actionGeneration,
        )
        val system = systemFactory.create(systemDefinition)
        try {
            generatedDeclarations.addAll(system.generateActions())
        } catch (e: LateDetectUnsupportedActionException) {
            throw ActionGenerationException("Unsupported action generated in generation $generationIndex", e)
        } catch (e: Exception) {
            throw ActionGenerationException("Error in declaration action generation $generationIndex", e)
        } finally {
            system.destroy()
        }
    }

    private fun checkForUnsupported(actionGenerators: List<ActionGenerator>) {
        if (actionGenerators.filterIsInstance<UnsupportedActionGenerator>().isNotEmpty()) {
            throw ActionGenerationException("Unsupported action generator present")
        }
    }

    private fun applyScenarioCommands() {
        val actionFactory = systemFactory.actionFactory
        val commands = scenarioCommands(actionFactory)
        val systemDefinition = SystemDefinition(
            declarations = scenarioDeclarations(actionFactory) + generatedDeclarations.map { it(actionFactory) },
            commands = commands,
        )
        system = systemFactory.create(systemDefinition)
        unappliedCommands.addAll(commands)
    }

    fun generateCommands(actionGenerators: List<ActionGenerator>) {
        checkForUnsupported(actionGenerators)
        system.let {
            try {
                val actionFactory = systemFactory.actionFactory
                if (it is MutableSystem<*, *>) {
                    it.applyIteration(
                        SystemIteration(commands = unappliedCommands, actionGenerators = actionGenerators)
                    )
                } else {
                    val systemDefinition = SystemDefinition(
                        declarations = scenarioDeclarations(actionFactory) + generatedDeclarations.map {
                            it(actionFactory)
                        },
                        commands = scenarioCommands(actionFactory) + generatedCommands.map { it(actionFactory) },
                        actionGenerators = actionGenerators,
                    )
                    system = systemFactory.create(systemDefinition)
                }
                val blockCommandCreators = system!!.generateActions()
                generatedCommands.addAll(blockCommandCreators)
                unappliedCommands.clear()
                unappliedCommands.addAll(blockCommandCreators.map { it(actionFactory) })
            } catch (e: LateDetectUnsupportedActionException) {
                throw ActionGenerationException("Unsupported action generated in generation $generationIndex", e)
            } catch (e: Exception) {
                throw ActionGenerationException("Error in command action generation $generationIndex", e)
            }
        }

    }

    fun getGeneratedActions() = GeneratedActions(generatedDeclarations, generatedCommands)

    fun System<AF, CF>.destroy() {
        if (this is MutableSystem<*, *>) {
            this.destroy()
        }
    }

    fun destroy() {
        system?.destroy()
    }

    private class ActionGenerationException(msg: String, e: Exception? = null) : RuntimeException(msg, e)

    companion object {
        private val Log = LoggerFactory.getLogger(ActionGeneratingSystemCursor::class.java)
    }
}

internal data class GeneratedActions<AF : ActionFactory>(
    val declarationCreators: List<(AF) -> Action>,
    val commandCreators: List<(AF) -> Action>
)
