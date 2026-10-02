package com.anaplan.engineering.azuki.script.formatter

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.ServiceLoader

/**
 * Formats scenarios using the 'default' [ScenarioFormatterService].
 *
 * This object will delegate to the first formatting service available over the service loader infrastructure that
 * reports that it [canFormat] scenarios.  Should none be available, it behaves like a [NoScenarioFormatterService].
 *
 * While this object implements [ScenarioFormatterService], it is not intended to be used as a service loader service
 * itself, and does this mainly for convenience and to keep the API in sync.
 *
 * This object caches its underlying service.
 */
object ScenarioFormatter : ScenarioFormatterService {

    /** Formats a scenario using the currently-loaded service. */
    override fun formatScenario(scenarioText: String) = service.formatScenario(scenarioText)

    /** Will the currently configured service actually perform formatting? */
    override val canFormat get() = service.canFormat

    /** Gets the underlying formatting service. */
    val service by lazy { getFirstViableService() ?: fallback() }

    private fun getFirstViableService() =
        ServiceLoader.load(ScenarioFormatterService::class.java).firstOrNull { it.canFormat }

    private fun fallback() = NoScenarioFormatterService.also {
        Log.warn(listOf(
            "No scenario formatters were found on your classpath, deactivating all scenario formatting.",
            "In most cases, adding a runtime dependency to azuki-script-formatter-ktlint should work.",
            "Otherwise, add an alternative ScenarioFormatterService implementation to your classpath/manifest.",
        ).joinToString(separator = "  "))
    }

    private val Log: Logger = LoggerFactory.getLogger(ScenarioFormatter::class.java)
}
