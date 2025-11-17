package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import java.util.*

object TicTacToeRunnableScenarioClassGenerator {

    // TODO - format class
    fun generate(
        className: String = "Generated_" + UUID.randomUUID().toString().replace("-", "_"),
        packageName: String? = null,
        implementationVersions: Map<String, String> = emptyMap(),
        scenarioScript: String,
    ) = RunnableScenarioClass(className, packageName, """
package ${packageName ?: ""}

${ScenarioParsingContext().apply { addTicTacToeDefaultImports() }.toImportString()}
import com.anaplan.engineering.azuki.core.runner.*
import com.anaplan.engineering.azuki.core.system.*
${if (implementationVersions.isEmpty()) "" else "import com.anaplan.engineering.azuki.core.scenario.Since"}
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeRunnableScenario

class $className : TicTacToeRunnableScenario() {

    @GeneratedScenario
    ${
        if (implementationVersions.isEmpty()) "" else "@Since(${
            implementationVersions.map { "ImplementationVersion(\"${it.key}\", \"${it.value}\")" }.joinToString(", ")
        })"
    }
    fun test() {
        $scenarioScript
    }
}
""")
}

data class RunnableScenarioClass(val className: String, val packageName: String?, val definition: String)

