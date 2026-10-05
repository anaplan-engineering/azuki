package com.anaplan.engineering.azuki.script.formatter

/**
 * Doesn't do any formatting.
 *
 * This will get invoked by `ScenarioFormatter` if no formatter is loaded but one is requested.
 * You can also pass it to something that expects a scenario formatter to turn formatting off.
 */
object NoScenarioFormatterService : ScenarioFormatterService {

    override fun formatScenario(scenarioText: String) = scenarioText

    override val canFormat = false
}
