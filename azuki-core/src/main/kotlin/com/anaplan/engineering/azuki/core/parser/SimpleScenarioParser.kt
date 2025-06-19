package com.anaplan.engineering.azuki.core.parser

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import java.util.concurrent.locks.ReentrantLock
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class SimpleScenarioParser<S : BuildableScenario<*>> : ScenarioParser<S> {

    private val engine by lazy {
        BasicJvmScriptingHost()
    }

    private val lock = ReentrantLock()

    override fun parse(
        scenarioString: String, requiredImports: String
    ): S = parse(scenarioString) {
        requireImportsFromString(requiredImports)
    }

    override fun parse(
        scenarioString: String,
        init: ScenarioParsingContext.() -> Unit
    ): S {
        val script = scenarioString.toScriptSource()
        val context = ScenarioParsingContext().apply(init)

        // TODO: do we need this lock for the Kotlin host?
        val evalResult = try {
            lock.lock()
            engine.evalWithTemplate<SimpleScenario>(script, {
                defaultImports(context.imports)
            })
        } finally {
            lock.unlock()
        }

        val returnValue = evalResult.valueOrThrow().returnValue
        if (returnValue !is ResultValue.Value) {
            throw IllegalArgumentException("Script did not return a value")
        }

        val scenario = returnValue.value
        if (scenario !is BuildableScenario<*>) {
            throw IllegalArgumentException("Script does not evaluate to scenario")
        }
        @Suppress("UNCHECKED_CAST") return scenario as S
    }

}

@KotlinScript(fileExtension = "scn.kts", compilationConfiguration = SimpleScenarioCompilationConfiguration::class)
abstract class SimpleScenario

object SimpleScenarioCompilationConfiguration : ScriptCompilationConfiguration({
    jvm {
        // Extract the whole classpath from context classloader and use it as dependencies
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
}) {
    private fun readResolve(): Any = SimpleScenarioCompilationConfiguration
}
