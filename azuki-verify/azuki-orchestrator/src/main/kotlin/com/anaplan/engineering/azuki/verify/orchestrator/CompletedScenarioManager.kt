package com.anaplan.engineering.azuki.verify.orchestrator

import com.anaplan.engineering.azuki.verify.batch.api.CompletedScenario
import com.anaplan.engineering.azuki.verify.batch.api.OrchestratableScenario
import com.anaplan.engineering.azuki.verify.batch.api.OrchestratableScenarioBatch
import com.anaplan.engineering.azuki.verify.batch.api.ScenarioResultContext
import com.anaplan.engineering.azuki.verify.orchestrator.configuration.RunConfiguration
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory

internal class CompletedScenarioManager<OS : OrchestratableScenario, RC : ScenarioResultContext>(
    private val runConfiguration: RunConfiguration,
    private val scenarioBatch: OrchestratableScenarioBatch<OS, RC>,
) {

    internal suspend fun completeScenarios(completedChannel: Channel<CompletedScenario<OS, RC>>) {
        for (completedScenario in completedChannel) {
            Log.debug("Completed scenario name=${completedScenario.orchestratableScenario.name}")
            scenarioBatch.complete(completedScenario, runConfiguration.runDir)
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(CompletedScenarioManager::class.java)
    }

}
