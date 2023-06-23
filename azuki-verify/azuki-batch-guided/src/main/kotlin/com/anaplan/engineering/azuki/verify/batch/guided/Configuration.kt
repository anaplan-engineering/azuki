package com.anaplan.engineering.azuki.verify.batch.guided

import java.io.File

data class RunConfiguration(
    val reps: Int = 1,
    val duration: Int? = null, // number of minutes
    val scenarios: ScenarioConfiguration,
)

data class ScenarioConfiguration(
    val files: List<File>,
    val abandon: AbandonConfiguration = AbandonConfiguration()
)

data class AbandonConfiguration(
    val minAttempts: Int = 10,
    val pcErrors: Int = 90
)


