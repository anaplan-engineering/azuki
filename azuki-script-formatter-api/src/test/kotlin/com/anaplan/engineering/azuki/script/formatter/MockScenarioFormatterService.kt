package com.anaplan.engineering.azuki.script.formatter

/**
 * Used to check for skip-over on selecting scenario formatters.
 */
class MockScenarioFormatterService : ScenarioFormatterService {

    override fun formatScenario(scenarioText: String) = error("shouldn't actually be used")

    override val canFormat = false
}
