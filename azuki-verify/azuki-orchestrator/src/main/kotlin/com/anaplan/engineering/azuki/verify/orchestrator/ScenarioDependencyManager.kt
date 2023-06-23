package com.anaplan.engineering.azuki.verify.orchestrator

import com.anaplan.engineering.azuki.runner.ExitCode
import com.anaplan.engineering.azuki.verify.batch.api.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.*

internal class ScenarioDependencyManager<OS : OrchestratableScenario, RC : ScenarioResultContext>(
    private val scenarioBatch: OrchestratableScenarioBatch<OS, RC>,
) {

    private suspend fun handleReady(scenario: OS, readyChannel: Channel<OS>) {
        Log.info("scenario=${scenario.name}, action=markReady")
        readyChannel.send(scenario)
    }

    private suspend fun handleFailedDependency(scenario: OS, completedChannel: Channel<CompletedScenario<OS, RC>>) {
        Log.info("scenario=${scenario.name}, action=invalidate, reason=failedDependency")
        completedChannel.send(CompletedScenario(scenario, ExitCode.FailedDependency))
    }

    @ExperimentalCoroutinesApi
    internal suspend fun manageScenarioDependencies(
        generatedChannel: Channel<OS>,
        readyChannel: Channel<OS>,
        completedChannel: Channel<CompletedScenario<OS, RC>>
    ) {
        val waiting = LinkedList<OS>()

        while (!generatedChannel.isClosedForReceive || !waiting.isEmpty()) {
            Log.info("New dependency management cycle, waitingCount=${waiting.size}")
            try {
                if (waiting.isNotEmpty()) {
                    val waitingByStatus = waiting.groupBy { scenarioBatch.getScenarioStatus(it) }
                    waiting.retainAll(waitingByStatus[ScenarioStatus.NotReady] ?: emptyList())

                    (waitingByStatus[ScenarioStatus.Ready] ?: emptyList()).forEach { handleReady(it, readyChannel) }
                    (waitingByStatus[ScenarioStatus.FailedDependency]
                        ?: emptyList()).forEach { handleFailedDependency(it, completedChannel) }
                }

                generatedChannel.consumeEach { scenario ->
                    when (scenarioBatch.getScenarioStatus(scenario)) {
                        ScenarioStatus.Ready -> handleReady(scenario, readyChannel)
                        ScenarioStatus.NotReady -> waiting.add(scenario)
                        ScenarioStatus.FailedDependency -> handleFailedDependency(scenario, completedChannel)
                    }
                }
            } catch (e: Exception) {
                // TODO -- proper exception handling
                Log.error("Unexpected exception", e)
            }
            delay(2000L)
        }

        Log.info("All required scenarios ready -- closing ready channel")
        readyChannel.close()
    }

    companion object {
        val Log = LoggerFactory.getLogger(ScenarioDependencyManager::class.java)
    }

}
