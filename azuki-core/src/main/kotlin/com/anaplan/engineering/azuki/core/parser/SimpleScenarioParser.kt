package com.anaplan.engineering.azuki.core.parser

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass
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
    ): S = parse(scenarioString, requiredImportLines = requiredImports.split("\n").map {
        it.trim().removePrefix("import").trim()
    }.filter { it.isNotBlank() }.toList())

    fun parse(
        scenarioString: String,
        requiredImportLines: List<String> = listOf(),
        requiredImportClasses: List<KClass<*>> = listOf(),
    ): S {
        val script = scenarioString.toScriptSource()

        // TODO: do we need this lock for the Kotlin host?
        val evalResult = try {
            lock.lock()
            engine.evalWithTemplate<SimpleScenario>(script, {
                defaultImports(requiredImportLines)
                defaultImports(*requiredImportClasses.toTypedArray())
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
