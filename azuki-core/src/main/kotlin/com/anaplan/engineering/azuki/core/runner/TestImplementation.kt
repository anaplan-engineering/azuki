package com.anaplan.engineering.azuki.core.runner

internal annotation class TestImplementation(val name: String, val version: String)
internal annotation class PersistenceImplementation(val name: String, val version: String)
internal annotation class ScenarioInfo(
    val type: JUnitScenarioType,
    val knownBug: Boolean,
    val toBeDone: Boolean,
    val issues: Array<String>
)
