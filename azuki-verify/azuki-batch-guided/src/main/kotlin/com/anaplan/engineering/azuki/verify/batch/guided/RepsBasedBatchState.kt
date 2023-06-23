package com.anaplan.engineering.azuki.verify.batch.guided

import com.anaplan.engineering.azuki.runner.ExitCode
import com.anaplan.engineering.azuki.verify.batch.api.CompletedScenario
import com.anaplan.engineering.azuki.verify.batch.api.ScenarioResultContext
import java.io.File


class RepsBasedBatchState<RC : ScenarioResultContext>(runConfiguration: RunConfiguration) :
    GuidedBatchState<RC>(runConfiguration) {

    private var baseScenarioToVerifiedCount = runConfiguration.scenarios.files.associateWith { 0 }
    override val baseScenarios = baseScenarioToVerifiedCount.keys

    override fun nextBaseScenarios(): List<File> {
        val tbd =
            baseScenarioToVerifiedCount.filter { (_, count) -> count <= runConfiguration.reps }.keys - abandonedBaseScenarios
        return if (tbd.isEmpty()) {
            emptyList()
        } else {
            tbd.toList()
        }
    }

    override fun complete(completedScenario: CompletedScenario<GuidedScenario, RC>) {
        val baseFile = completedScenario.orchestratableScenario.baseFile
        completed += completedScenario
        if (completedScenario.hasError()) {
            abandonIfThresholdMet(baseFile)
        } else if (completedScenario.exitCode == ExitCode.Ok) {
            baseScenarioToVerifiedCount += (baseFile to baseScenarioToVerifiedCount[baseFile]!! + 1)
        }
    }

    override fun pcComplete(): Int {
        val runningBaseScenariosToVerifiedCounts = baseScenarioToVerifiedCount - abandonedBaseScenarios
        val required = runningBaseScenariosToVerifiedCounts.keys.size * runConfiguration.reps
        val completed = runningBaseScenariosToVerifiedCounts.values.sum()
        return if (required == 0) 100 else (completed * 100) / required
    }

}
