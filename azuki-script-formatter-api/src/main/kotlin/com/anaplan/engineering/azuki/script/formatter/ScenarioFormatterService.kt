package com.anaplan.engineering.azuki.script.formatter

/**
 * A pluggable scenario formatter.
 *
 * This interface forms the service loader contract for a scenario formatter, but is also useful for abstracting over
 * formatters in other places.
 */
fun interface ScenarioFormatterService {

    /** Formats a scenario. */
    fun formatScenario(scenarioText: String): String

    /**
     * Whether this formatter is actually able to format anything.
     *
     * If not, scenarios will be passed through unchanged.
     */
    val canFormat: Boolean get() = true
}
