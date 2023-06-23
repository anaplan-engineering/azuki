package com.anaplan.engineering.azuki.verify.batch.guided

import com.anaplan.engineering.azuki.runner.ExitCode
import com.anaplan.engineering.azuki.verify.batch.api.CompletedScenario
import com.anaplan.engineering.azuki.verify.batch.api.ScenarioResultContext
import org.slf4j.LoggerFactory
import java.io.File

abstract class GuidedBatchState<RC : ScenarioResultContext>(protected val runConfiguration: RunConfiguration) {
    abstract val baseScenarios: Collection<File>
    abstract fun nextBaseScenarios(): List<File>
    abstract fun complete(completedScenario: CompletedScenario<GuidedScenario, RC>)
    abstract fun pcComplete(): Int

    var completed = emptyList<CompletedScenario<GuidedScenario, RC>>()
        protected set

    protected var abandonedBaseScenarios = emptyList<File>()

    private val abandonConfiguration = runConfiguration.scenarios.abandon

    protected fun abandonIfThresholdMet(baseFile: File) {
        val completedForBase = completed.filter { it.orchestratableScenario.baseFile == baseFile }
        val runCount = completedForBase.count()
        val errorCount = completedForBase.count { it.hasError() }
        val errorPc = (100 * errorCount) / runCount
        Log.debug("Scenario had error check if abandon threshold met: baseFile=$baseFile runCount=$runCount errorPc=$errorPc")
        if (runCount >= abandonConfiguration.minAttempts && errorPc >= abandonConfiguration.pcErrors) {
            Log.warn("Abandoning base scenario after error threshold met: baseFile=$baseFile runCount=$runCount errorPc=$errorPc")
            abandonedBaseScenarios += baseFile
        }
    }

    protected fun CompletedScenario<GuidedScenario, RC>.hasError() = exitCode.category == ExitCode.Category.Error

    companion object {
        private val Log = LoggerFactory.getLogger(GuidedBatchState::class.java)
    }
}
