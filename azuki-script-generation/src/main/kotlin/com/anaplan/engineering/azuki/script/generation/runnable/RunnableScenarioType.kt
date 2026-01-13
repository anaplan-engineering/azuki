package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.AdapterTest
import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.GeneratedScenario
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier

/**
 * Represents one of the mutually exclusive annotations capturing the high-level 'type' of a scenario method.
 *
 * This is left open to allow type aliases and other specializations of the scenario types to have special handling.
 */
interface RunnableScenarioMethodType {

    /**
     * The annotation that should be emitted on the scenario method.
     */
    val annotation: Annotation

    /**
     * The name that should be used to import or refer to the annotation.
     */
    val kotlinName: QualifiedIdentifier
        get() = annotation.annotationClass.toQualifiedIdentifier()
}

data class AdapterTestMethodType(override val annotation: AdapterTest) : RunnableScenarioMethodType

data class AnalysisScenarioMethodType(override val annotation: AnalysisScenario) : RunnableScenarioMethodType

data class GeneratedScenarioMethodType(override val annotation: GeneratedScenario) : RunnableScenarioMethodType

data class EacMethodType(override val annotation: Eac) : RunnableScenarioMethodType

fun AdapterTest.toMethodType() = AdapterTestMethodType(this)
fun AnalysisScenario.toMethodType() = AnalysisScenarioMethodType(this)
fun GeneratedScenario.toMethodType() = GeneratedScenarioMethodType(this)
fun Eac.toMethodType() = EacMethodType(this)
