package com.anaplan.engineering.azuki.core.runner

data class TaskResult<T>(
    val taskName: String,
    val implName: String,
    val result: T? = null,
    val error: String? = null,
    val script: String? = null,
    val output: String? = null,
    val duration: Long? = null
) {
    fun durationMs() = if (duration == null) 0 else duration / 1000000
}
