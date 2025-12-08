package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.GeneratedScenario
import com.anaplan.engineering.azuki.script.generation.runnable.KotlinName.Companion.toClassName

/**
 * Represents one of the mutually exclusive annotations capturing the high-level 'type' of a scenario method.
 */
interface RunnableScenarioMethodType {

    /**
     * The annotation that should be emitted on the scenario method.
     */
    val annotation: Annotation

    val className: KotlinName
        get() = annotation.annotationClass.toClassName()
}

data class AnalysisScenarioMethodType(override val annotation: AnalysisScenario) : RunnableScenarioMethodType

data class GeneratedScenarioMethodType(override val annotation: GeneratedScenario) : RunnableScenarioMethodType

data class EacMethodType(override val annotation: Eac) : RunnableScenarioMethodType
