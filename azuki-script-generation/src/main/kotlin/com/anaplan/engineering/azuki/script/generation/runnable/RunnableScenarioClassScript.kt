package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.script.generation.VerifiableScenarioScript

/**
 * A script representation of an Azuki runnable scenario.
 */
data class RunnableScenarioClassScript(
    val testName: QualifiedIdentifier,
    val baseName: QualifiedIdentifier,
    val methods: List<MethodScript>,
    val beh: BEH? = null,
) {

    /**
     * A method in a runnable scenario script.
     */
    data class MethodScript(
        val name: String,
        val type: RunnableScenarioMethodType,
        val annotations: RunnableScenarioAnnotations,
        val body: VerifiableScenarioScript,
    )
}
