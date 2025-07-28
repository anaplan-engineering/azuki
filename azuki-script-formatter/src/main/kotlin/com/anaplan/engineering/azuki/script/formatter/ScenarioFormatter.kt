package com.anaplan.engineering.azuki.script.formatter

import com.pinterest.ktlint.rule.engine.api.*
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RuleExecution
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import java.io.File

object ScenarioFormatter {
    private val ruleProviders = StandardRuleSetProvider().getRuleProviders()

    private val editorConfigPropertyRegistry = EditorConfigPropertyRegistry(ruleProviders)

    private val editorConfigOverride = EditorConfigOverride.from(
        EXPERIMENTAL_RULES_EXECUTION_PROPERTY to RuleExecution.enabled,
        editorConfigPropertyRegistry.find("ktlint_standard_argument-list-wrapping") to RuleExecution.disabled,
    )

    private val ruleEngine = KtLintRuleEngine(
        ruleProviders = ruleProviders,
        editorConfigOverride = editorConfigOverride,
    )

    @JvmStatic
    fun formatScenario(scenarioText: String): String {
        val code = Code.fromSnippet(scenarioText, true)

        return ruleEngine.format(code) { error ->
            if (error.canBeAutoCorrected) {
                AutocorrectDecision.ALLOW_AUTOCORRECT
            } else {
                AutocorrectDecision.NO_AUTOCORRECT
            }
        }
    }
}

fun main(args: Array<String>) {
    val fileName = args[0]
    val scenarioText = File(fileName).readText()

    println(ScenarioFormatter.formatScenario(scenarioText))
}
