package com.anaplan.engineering.azuki.formatter

import com.pinterest.ktlint.KtlintCommandLine

import java.nio.file.Files

class ScenarioFormatter {

    static String formatScenario(String scenarioText) {
        File file = Files.createTempFile("azuki-", ".kts").toFile()
        // The ktLint formatter does not accept scripts which start with whitespace
        file.text = scenarioText.dropWhile { (it as Character).isWhitespace() }
        formatKotlinFile(file)
        return file.text.readLines()
        // remove blank lines
            .findAll { !it.isAllWhitespace() }
            .join("\n")
    }

    private static void formatKotlinFile(File file) {
        def ktlintCommand = new KtlintCommandLine()
        ktlintCommand.format = true
        ktlintCommand.patterns = [file.absolutePath]
        ktlintCommand.experimental = true
        ktlintCommand.disabledRules = "experimental:argument-list-wrapping"
        ktlintCommand.run()
    }

    static void main(String[] args) {
        def fileName = args[0]
        def scenarioText = new File(fileName).text
        println formatScenario(scenarioText)
    }

}
