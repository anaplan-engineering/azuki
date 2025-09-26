package com.anaplan.engineering.azuki.listeners

import com.anaplan.engineering.azuki.core.runner.AzukiRunListener
import com.anaplan.engineering.azuki.core.runner.ScenarioResult

class PrintingRunListener : AzukiRunListener {
    override fun scenarioComplete(scenarioResult: ScenarioResult) {
        println(scenarioResult)
    }
}
