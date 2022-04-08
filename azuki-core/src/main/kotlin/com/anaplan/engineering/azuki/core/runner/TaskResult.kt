package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario

data class TaskResult<S : BuildableScenario<*>, T>(
    val taskType: TaskType,
    val implName: String,
    val result: T? = null,
    val log: Log = Log(),
    val exception: Exception? = null,
    val scenario: S,
    val duration: Long? = null
)

data class Log(
    val output: String? = null,
    val error: String? = null,
)
