package com.anaplan.engineering.azuki.verify.batch.guided

import com.anaplan.engineering.azuki.verify.batch.api.CompletedScenario
import com.anaplan.engineering.azuki.verify.batch.api.ScenarioResultContext


class DurationBasedBatchState<RC : ScenarioResultContext>(runConfiguration: RunConfiguration) :
    GuidedBatchState<RC>(runConfiguration) {

    private val start = System.currentTimeMillis();

    private val durationMillis = runConfiguration.duration!! * 1000 * 60

    override val baseScenarios = runConfiguration.scenarios.files

    override fun nextBaseScenarios() = baseScenarios

    override fun complete(completedScenario: CompletedScenario<GuidedScenario, RC>) {
        completed += completedScenario
        if (completedScenario.hasError()) {
            abandonIfThresholdMet(completedScenario.orchestratableScenario.baseFile)
        }
    }

    override fun pcComplete() =
        if (baseScenarios == abandonedBaseScenarios) {
            100
        } else {
            val timePassed = (System.currentTimeMillis() - start)
            ((timePassed * 100) / durationMillis).toInt()
        }
}
