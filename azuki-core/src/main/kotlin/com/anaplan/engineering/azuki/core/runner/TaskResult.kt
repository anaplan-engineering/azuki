package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario

data class TaskResult<S: BuildableScenario<*>, T>(
    val taskName: String,
    val implName: String,
    val result: T? = null,
    val error: String? = null,
    val scenario: S,
    val output: String? = null,
    val duration: Long? = null
) {
    fun durationMs() = if (duration == null) 0 else duration / 1000000
}
