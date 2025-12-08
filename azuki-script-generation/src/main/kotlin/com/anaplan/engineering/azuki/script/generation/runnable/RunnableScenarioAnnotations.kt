package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.GeneratedScenario
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.scenario.Since
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KFunction

/**
 * Collects relevant annotations for a scenario method.  This lets us later reconstruct them in a canonical order.
 */
data class RunnableScenarioAnnotations(
    val knownBug: KnownBug? = null,
    val since: Since? = null,
    // TODO: others
) {

    operator fun plus(annotation: Annotation) = when (annotation) {
        // none of these are repeatable annotations, so we don't need to check for repetitions
        is KnownBug -> copy(knownBug = annotation)
        is Since -> copy(since = annotation)
        is AnalysisScenario, is GeneratedScenario, is Eac -> this
        else -> {
            Log.warn(
                "Not sure what to do with annotations of type {}, ignoring", annotation.annotationClass.simpleName
            )
            this
        }
    }

    companion object {

        private val Log: Logger = LoggerFactory.getLogger(RunnableScenarioAnnotations::class.java)

        fun KFunction<*>.scenarioAnnotations(): RunnableScenarioAnnotations =
            annotations.fold(RunnableScenarioAnnotations()) { acc, x -> acc + x }
    }
}
