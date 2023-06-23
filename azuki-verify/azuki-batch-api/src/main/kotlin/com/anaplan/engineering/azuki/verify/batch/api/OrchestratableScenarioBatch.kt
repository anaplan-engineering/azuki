package com.anaplan.engineering.azuki.verify.batch.api

import java.io.File

interface OrchestratableScenarioBatch<OS : OrchestratableScenario, RC : ScenarioResultContext> {

    /* May return more than one scenario where there are dependencies */
    fun nextScenario(): List<OS>

    fun complete(completed: CompletedScenario<OS, RC>, runDir: File)

    fun createProgressReport(runDir: File, fileName: String): File

    fun pcComplete(): Int

    fun getScenarioStatus(scenario: OS): ScenarioStatus

    /**
     * Called before the batch is started to enable implementor to perform additional
     * set-up, environmental checks etc.
     */
    fun beforeBatch() {}

    /**
     * Called after the catch is completed to enabled implementor to produce additional
     * reports etc.
     */
    fun afterBatch() {}
}

enum class ScenarioStatus {
    Ready,
    NotReady,
    FailedDependency
}


