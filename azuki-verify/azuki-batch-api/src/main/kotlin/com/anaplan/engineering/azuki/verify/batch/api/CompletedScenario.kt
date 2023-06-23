package com.anaplan.engineering.azuki.verify.batch.api

import com.anaplan.engineering.azuki.runner.ExitCode


class CompletedScenario<OS : OrchestratableScenario, RC : ScenarioResultContext>(
    val orchestratableScenario: OS,
    val exitCode: ExitCode,
    val context: RC? = null,
    val error: Exception? = null,
)
