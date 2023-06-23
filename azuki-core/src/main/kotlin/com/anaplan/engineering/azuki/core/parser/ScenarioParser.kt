package com.anaplan.engineering.azuki.core.parser

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario

interface ScenarioParser<S: BuildableScenario<*>> {

    fun parse(
        scenarioString: String,
        requiredImports: String
    ): S

}
